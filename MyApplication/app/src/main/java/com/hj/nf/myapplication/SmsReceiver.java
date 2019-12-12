package com.hj.nf.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by snell1 on 2017-03-07.
 */

// 받는순간 db에 넣고 수신 이벤트 처리까지 해두긴했는데 mms는 구조가 상당히 어렵다
public class SmsReceiver extends BroadcastReceiver {

    private MySQLiteOpenHelper mDbOpenHelper;

    @Override
    public void onReceive(Context context, Intent intent) {

        mDbOpenHelper = new MySQLiteOpenHelper(context);
        mDbOpenHelper.open();



        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        if (bundle != null)
        {
            //sms 수신부
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i=0; i<msgs.length; i++){
                Uri uri = Telephony.Sms.Inbox.CONTENT_URI;

                //메세지 생성
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);



                ContentValues messageValues = new ContentValues();

                //내용
                messageValues.put("body", msgs[i].getMessageBody());
                //시간
                messageValues.put("date",msgs[i].getTimestampMillis());
                //번호
                messageValues.put("address",msgs[i].getOriginatingAddress());
                context.getContentResolver().insert(uri,messageValues);

                ////////////////////////////////

                Uri allMessage = Uri.parse("content://sms");
                ContentResolver sms = context.getContentResolver();

                Cursor c = sms.query(allMessage,
                        new String[]{"_id", "thread_id", "address", "person", "date", "body",
                                "protocol", "read", "status", "type", "reply_path_present",
                                "subject", "service_center", "locked", "error_code", "seen"},
                        null, null,
                        "date DESC");
                Cursor Update_sms = mDbOpenHelper.getSmscolumns();


                while (c.moveToNext()) {

                    //DB에 등록된 번호
                    String find_smsnum = c.getString(2);
                    //DB에 등록된 번호로 온 문자의 시간
                    long find_smstime = c.getLong(4);

                    boolean isdone = false;

                    Log.d("확인 DB시간", "" + find_smstime);
                    Log.d("확인 받은시간", "" + msgs[i].getTimestampMillis());

                    //받은 시간과 현재 시간의 차이가 2초일때
                    if(find_smstime+2000 > msgs[i].getTimestampMillis()) {
                        while (Update_sms.moveToNext()){

                            Log.d("진입 DB번호", "" + "0" + Update_sms.getString(Update_sms.getColumnIndex("s_num")));
                            Log.d("진입 받은번호", "" + find_smsnum);

                            //새로온 문자까지 조회했을 때 sms정보를 가지고 db에 넣기
                            if(("0"+Update_sms.getString(Update_sms.getColumnIndex("s_num"))).equals(find_smsnum)) {
                                Log.d("확인 DB번호", "" + "0" + Update_sms.getString(Update_sms.getColumnIndex("s_num")));
                                Log.d("확인 받은번호", "" + find_smsnum);
                                Log.d("확인 DB시간", "" + find_smstime);
                                Log.d("확인 받은시간", "" + msgs[i].getTimestampMillis());


                                    ///////    SMS DB 인덱스 번호  -  위의 query 내 String array 내용과 맵핑

                                    int threadId = c.getInt(1);
                                    String address = c.getString(2);
                                    String Name = c.getString(3);
                                    String Date = c.getString(4);
                                    String Body = c.getString(5);
                                    String Type = c.getString(9);
                                    int read = c.getInt(7);

                                    mDbOpenHelper.insert_sms(address, Name, Date, Body, Type, threadId, read);
                                    Log.d("문자 DB에 추가", "" + threadId);
                                isdone = true;
                                break;



                            }
                        }
                    }


                if(isdone)
                    break;
                }

            }
            //---display the new SMS message---
            //todo 알림
       //     Toast.makeText(context, str, Toast.LENGTH_SHORT).show();


            //APP 푸시 알림
            NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, SmsReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.drawable.msg).setWhen(System.currentTimeMillis())
                    .setNumber(1).setContentTitle("새 메세지")
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pendingIntent).setAutoCancel(true);

            notificationmanager.notify(1, builder.build());





        }
    }



}