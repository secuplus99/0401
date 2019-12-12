package com.hj.nf.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by snell1 on 2017-02-17.
 */


//TODO 볼륨 버튼 이벤트
public class RemoteControlReceiver extends BroadcastReceiver {

    private static long prevTime;
    private static boolean isSingleCall = false;
    private MySQLiteOpenHelper mDbOpenHelper; // db


    @Override
    public void onReceive(Context context, Intent intent) {
        mDbOpenHelper = new MySQLiteOpenHelper(context);
        mDbOpenHelper.open();
        //db열기

        if (intent != null) {
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                int prevVolume = intent.getExtras().getInt("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", 0); //그전의 볼륨
                int currentValue = intent.getExtras().getInt("android.media.EXTRA_VOLUME_STREAM_VALUE", 0); //현재볼륨


                AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                boolean volumeAction = false;
                boolean volumeAction2 = false;

                if ((currentValue == 0 && prevVolume == 0) ) {
                    if (!isSingleCall) {
                        isSingleCall = true; // 볼륨 체인지를 인식하기전까지 시간 버는 call
                    } else {
                        isSingleCall = false;
                        volumeAction2 = true;   // 이전이 최소 현재도 최소 > 볼륨이 최소이고 그다음 누른것도 최소일때
                    }
                }
                if (currentValue == maxVolume && prevVolume == maxVolume) {
                    if (!isSingleCall) {
                        isSingleCall = true;
                    } else {
                        isSingleCall = false;
                        volumeAction = true;   //볼륨이 맥스 그다음 이벤트도 맥스일경우
                    }
                }

                if (volumeAction2) // 최소 최소 일때
                {    // 버튼을 연속으로 누른텀이 200이하일때 (0.2초)
                    if (System.currentTimeMillis() - prevTime < 200) {

                        Toast toast1 = Toast.makeText(context, "앱 실행", Toast.LENGTH_SHORT);toast1.show();

                        //앱실행
                        Intent launch_intent = new  Intent(context, MainActivity.class);
                        //컴포넌트 설정
                        launch_intent.setComponent(new ComponentName("com.hj.nf.myapplication","com.hj.nf.myapplication.MainActivity"));
                        launch_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        launch_intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        context.startActivity(launch_intent);


                    }
                    else {
                      //  Log.d("", "느리게 누른경우");
                    }
                    prevTime = System.currentTimeMillis();
                }


                if (volumeAction) //볼륨이 최대 최대 일경우
                {    //연속누름의 경우
                    if (System.currentTimeMillis() - prevTime < 200) {
                        //퍼미션
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {return;}

                        //todo call log 삭제

                        Cursor test1 = mDbOpenHelper.getCallcolumns(); //call log db 받기
                        while (test1.moveToNext()) {
                            context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, "0"+test1.getString( test1.getColumnIndex("c_num")), null);
                        }
                        test1.moveToFirst();

                        //todo sms mms 모두 삭제

                        Cursor test2 = mDbOpenHelper.getSmscolumns(); //sms db 받기
                        while (test2.moveToNext()) {

                            String test123 = test2.getString(test2.getColumnIndex("s_num")); //db를 통해 번호를 얻고
                            int thread1 = getThread1(context, test123); // 그 번호를 통해 getThread1 함수를 통해 thread_id를 얻는다
                            context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" +  thread1), null, null);
                        }
                        test2.moveToFirst();

                        //todo 연락처 삭제
                        Cursor test3 = mDbOpenHelper.getNumbercolumns();
                        while (test3.moveToNext()) {
                            DeleteContact(context, "0" + test3.getString(test3.getColumnIndex("n_num")), test3.getString(test3.getColumnIndex("n_name")));
                        }
                        test3.moveToFirst();



                        Toast toast1 = Toast.makeText(context, "삭제!", Toast.LENGTH_SHORT);
                        toast1.show();


                    }
                    else {
                        Log.d("", "느리게 누른경우");
                    }

                    prevTime = System.currentTimeMillis();

                }
            }
        }

    }
    private int getThread1(Context context, String addr) {

        Cursor c = context.getContentResolver().query(Uri.parse("content://sms"), new String[]{"thread_id", "address"},
                null, null, "date DESC");

        int thread_id = 0;
        while (c.moveToNext()) {
            String find_smsnum = c.getString(1);

            if (find_smsnum.equals("0"+addr)) {
                thread_id = c.getInt(0);
                break;
            }
        }
        c.close();

        return thread_id;
    }

    public boolean DeleteContact(Context context, String phone, String name) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor cur = context.getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        context.getContentResolver().delete(uri, null, null);
                        return true;
                    }

                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        } finally {
            cur.close();
        }
        return false;
    }
}