package com.hj.nf.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by snell1 on 2017-03-06.
 */

public class act_recover extends Activity {

    @BindView(R.id.btn_call)
    Button btnCall;
    @BindView(R.id.btn_file)
    Button btnFile;
    @BindView(R.id.title_icon)
    ImageView titleIcon;
    @BindView(R.id.title_btn)
    ImageView titleBtn;
    private MySQLiteOpenHelper mDbOpenHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_recover);
        ButterKnife.bind(this);
        mDbOpenHelper = new MySQLiteOpenHelper(this);
        mDbOpenHelper.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbOpenHelper.close();
    }

    //todo 연락처 복구
    public void recover_call() {
        Cursor mCursor;
        mCursor = mDbOpenHelper.getCallcolumns();
        while (mCursor.moveToNext()) {
            Save_CallLog(mCursor.getString(mCursor.getColumnIndex("c_num")), mCursor.getString(mCursor.getColumnIndex("c_name")),
                    mCursor.getString(mCursor.getColumnIndex("c_date")), mCursor.getString(mCursor.getColumnIndex("c_duration")),
                    mCursor.getString(mCursor.getColumnIndex("c_type")));
        }
    }
    //복구할 연락처 저장
    public void Save_CallLog(String number, String name, String date, String dur, String type) {
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, "0" + number); //번호 0으로 시작하는 string 일경우 0이 인식이 안되어 0을 추가
        values.put(CallLog.Calls.DATE, date); // 시간
        values.put(CallLog.Calls.DURATION, dur); // 통화 시간
        values.put(CallLog.Calls.TYPE, type); // 통화 속성 ( 부재중, 발신, 수신, 실패 등)
        values.put(CallLog.Calls.CACHED_NAME, name); // 이름

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        } // 퍼미션 유무
        this.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values); // 추가
    }

    //todo sms 복구
    public void recover_sms() {

        //4.4 이상일 경우 디폴트앱 설정 퍼미션 요청 및 이벤트 실행
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();
            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) { // 디폴트앱으로 설정이 되어 있지 않다면

                // 디폴트앱 설정화면으로
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
                startActivityForResult(intent, 1);

            }
            else { // 디폴트앱으로 걸려있다면
                Cursor smsCursor;
                smsCursor = mDbOpenHelper.getSmscolumns();
                while (smsCursor.moveToNext()) {

                    Save_Sms(smsCursor.getString(smsCursor.getColumnIndex("s_num")), smsCursor.getString(smsCursor.getColumnIndex("s_name")),
                            smsCursor.getString(smsCursor.getColumnIndex("s_date")), smsCursor.getString(smsCursor.getColumnIndex("s_body")),
                            smsCursor.getString(smsCursor.getColumnIndex("s_type")), smsCursor.getInt(smsCursor.getColumnIndex("s_thread_id")),
                            smsCursor.getInt(smsCursor.getColumnIndex("s_read")), "inbox");

                }
            }
        } else {
            //  4.4 이하일경우
        }


    }

    //복구할 sms 저장
    public boolean Save_Sms(String number, String name, String date, String body, String type, int thread_id, int read, String folderName) {
        boolean ret = false;
        try {
            ContentValues values = new ContentValues();
            values.put("address", "0" + number);// 번호
            values.put("body", body);  //내용
            values.put("type", type);  //수, 발신 타입
            values.put("date", date);  //시간
            values.put("read", read);  //읽음, 안 읽음


            // 4.4 보다 크다면 디폴트 앱이라면
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Uri uri = Telephony.Sms.Sent.CONTENT_URI;
                if (folderName.equals("inbox")) {
                    uri = Telephony.Sms.Inbox.CONTENT_URI;
                }
                // 문자 삽입
                getContentResolver().insert(uri, values);
            }
            // 4.4보다 작을경우 / 없어도 되는 구문
            else {
                getContentResolver().insert(Uri.parse("content://sms/" + folderName), values);
            }

            ret = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            ret = false;
        }
        return ret;
    }


    // 문자가 디폴트앱으로 설정되어 있지 않다면 / 디폴트앱 받기 설정.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    final String myPackageName = getPackageName();

                    // 디폴트앱 승인이후 > recover_sms에서 수행 못 한 이벤트 처리
                    if (Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
                        Cursor smsCursor;
                        smsCursor = mDbOpenHelper.getSmscolumns();
                        while (smsCursor.moveToNext()) {

                            Save_Sms(smsCursor.getString(smsCursor.getColumnIndex("s_num")), smsCursor.getString(smsCursor.getColumnIndex("s_name")),
                                    smsCursor.getString(smsCursor.getColumnIndex("s_date")), smsCursor.getString(smsCursor.getColumnIndex("s_body")),
                                    smsCursor.getString(smsCursor.getColumnIndex("s_type")), smsCursor.getInt(smsCursor.getColumnIndex("s_thread_id")),
                                    smsCursor.getInt(smsCursor.getColumnIndex("s_read")), "inbox");


                        }
                    }
                }
            }
        }
    }



    //todo mms 복구
    public void recover_mms() {

        Cursor mmsCursor;
        mmsCursor = mDbOpenHelper.getMmscolumns();
        while (mmsCursor.moveToNext()) {

            //getThread1 함수를 통한 thread_id 받기가 첫 번째 인자.
            Save_Mms(getThread1(mmsCursor.getString(mmsCursor.getColumnIndex("m_num"))),
                    mmsCursor.getString(mmsCursor.getColumnIndex("m_date")),
                    mmsCursor.getString(mmsCursor.getColumnIndex("m_type")),
                    mmsCursor.getString(mmsCursor.getColumnIndex("m_body")),
                    mmsCursor.getString(mmsCursor.getColumnIndex("m_num")),
                    "제목없음");

        }
        mmsCursor.close();
    }

    //복구한 mms를 저장
    public Uri Save_Mms(int thread_id, String date1, String type1, String body1, String num1, String subject) {
        try {
            Uri destUri = Uri.parse("content://mms");

            // 더미 sms 생성 > 이후에 mms로 덮어씌우는 과정
            ContentValues dummyValues = new ContentValues();
            dummyValues.put("thread_id", thread_id);
            dummyValues.put("address", "0" + num1);
            dummyValues.put("body", "Dummy SMS body.");
            Uri dummySms = getContentResolver().insert(Uri.parse("content://sms/"), dummyValues);

            // 정보 등록 설정
            long now = System.currentTimeMillis();
            ContentValues mmsValues = new ContentValues();
            if (type1.equals("128"))
                mmsValues.put("msg_box", Telephony.Mms.MESSAGE_BOX_OUTBOX); // 메세지 발신쪽으로
            else if (type1.equals("132"))
                mmsValues.put("msg_box", Telephony.Mms.MESSAGE_BOX_INBOX);  // 메세지 수신쪽으로


            mmsValues.put("m_type", type1);        // 수, 발신 구분
            mmsValues.put("thread_id", thread_id); // thread_id
            mmsValues.put("date", date1);
//
            mmsValues.put("m_id", date1);     // 고유아이디 > 그 문자의 시간으로 대체
            mmsValues.put("read", 1);         // 무조건 읽음
            mmsValues.put("sub", subject);    //제목 > "제목없음" 으로 설정

            mmsValues.put("ct_t", "application/vnd.wap.multipart.related"); //mms일경우 ct_t 값
            mmsValues.put("m_cls", "personal");

            //기타 기본 설정
            mmsValues.put("sub_cs", 106);
            mmsValues.put("v", 19);
            mmsValues.put("pri", 129);
            mmsValues.put("tr_id", "T" + Long.toHexString(now));
            mmsValues.put("resp_st", 128);

            // mms로 집어넣기
            Uri res = getContentResolver().insert(destUri, mmsValues);
            String messageId = res.getLastPathSegment().trim();

            // mms는 내용과 어드레스를 읽거나 생성하는 매커니즘이 sms과는 다르기에 생성
            mms_createPart(messageId, body1);
            mms_createAddr(messageId, "0" + num1);

            // 더미를 삭제
            getContentResolver().delete(dummySms, null, null);

            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Uri mms_createPart(String id, String body1) throws Exception {
        ContentValues mmsPartValue = new ContentValues();
        mmsPartValue.put("mid", id);
        mmsPartValue.put("ct", "text/plain"); //txt타입
        mmsPartValue.put("text", body1);
        mmsPartValue.put("cid", "<" + System.currentTimeMillis() + ">"); //현재시간으로

        Uri partUri = Uri.parse("content://mms/" + id + "/part");
        Uri res = getContentResolver().insert(partUri, mmsPartValue); // 내용 삽입


        // data 생성부
//        OutputStream os = context.getContentResolver().openOutputStream(res);
//        ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
//        byte[] buffer = new byte[256];
//        for (int len=0; (len=is.read(buffer)) != -1;)
//        {
//            os.write(buffer, 0, len);
//        }
//        os.close();
//        is.close();

        return res;
    }

    private Uri mms_createAddr(String id, String addr) throws Exception {
        ContentValues addrValues = new ContentValues();
        addrValues.put("address", addr);  //주소값
        addrValues.put("charset", "106");
        addrValues.put("type", 151); // TO
        Uri addrUri = Uri.parse("content://mms/" + id + "/addr");
        Uri res = getContentResolver().insert(addrUri, addrValues);

        return res;
    }


    private int getThread1(String addr) {
        int thread_id = 0;

        Cursor c = getContentResolver().query(Uri.parse("content://sms"), new String[]{"thread_id", "address"}, null, null, "date DESC");

        while (c.moveToNext()) {
            String find_smsnum = c.getString(1);

            //내가 찾는 번호와 sms 기록중에 맞는 번호가 있을때
            if (find_smsnum.equals("0" + addr)) {

                //스레드 id를 얻는다
                thread_id = c.getInt(0);
                break;
            }
        }
        c.close();
        return thread_id;
    }


    //todo 연락처 복구
    public void recover_contact() {
        Cursor mCursor;
        mCursor = mDbOpenHelper.getNumbercolumns();

        while (mCursor.moveToNext()) {
            save_contact("0" + mCursor.getString(mCursor.getColumnIndex("n_num")), mCursor.getString(mCursor.getColumnIndex("n_name")));
        }
    }


    //복구할 연락처 저장
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public void save_contact(String num1, String name1) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        //연락처 list ops에 이름값 추가
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name1) // 이름값 추가
                .build());

        //연락처 list ops에 번호값 추가
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, num1) // 번호값 추가
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        try {
            //연락처 저장
            ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

        } catch (RemoteException e) {
            // error
        } catch (OperationApplicationException e) {
            // error
        }


    }



    //todo 파일 복구
    public void recover_file() {
        Cursor fCursor;
        fCursor = mDbOpenHelper.getFilecolumns();
        while (fCursor.moveToNext()) {

            //APP 내부 스토리지 패스 설정
            String app_inner_save_path = getString(R.string.hide_data);

            //movefile 함수를 통해 파일을 이동
            moveFile(app_inner_save_path + fCursor.getString(fCursor.getColumnIndex("f_filename")),
                    fCursor.getString(fCursor.getColumnIndex("f_filename")),
                    fCursor.getString(fCursor.getColumnIndex("f_path")));

            // 갤러리의 경우 바로 갱신이 안되기에 양쪽의 컨텐츠가 새로 추가되었음을 갱신
            refreshGallery(app_inner_save_path + fCursor.getString(fCursor.getColumnIndex("f_filename")));
            refreshGallery(fCursor.getString(fCursor.getColumnIndex("f_path")) + fCursor.getString(fCursor.getColumnIndex("f_filename")));
        }


    }


    private void moveFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 잘래니기 할 위치 in
            in = new FileInputStream(inputPath);
            // 잘라내기가 되어 복사될 위치 out > 사진의 원래 위치
            out = new FileOutputStream(outputPath + inputFile);

            //buffer로 in 파일을 읽고 out에  저장
            byte[] buffer = new byte[2048];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;

            // 앱 내부 메모리에 있던 파일 삭제
            new File(inputPath).delete();


        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    //갤러리 갱신
    public void refreshGallery(String fileUri) {

        File file = new File(fileUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            if (file.exists()) {
                //파일 위치에 파일이 존재할 경우 브로드캐스트를 날려서 갱신
                Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(new File(fileUri));
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);
            } else {
                // 파일 위치에 파일이 존재하지 않을 경우 삭제처리
                try {

                    getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA + "='"
                            + new File(fileUri).getPath() + "'", null);

                    getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA + "='"
                            + new File(fileUri).getPath() + "'", null);

                    getContentResolver().delete(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Images.Media.DATA + "='" + new File(fileUri).getPath() + "'", null);
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }
        //4.4 이하일경우
        else {
            //그냥 브로드 캐스트 날리면 됩니다
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @OnClick({R.id.btn_call, R.id.btn_file, R.id.title_icon, R.id.title_btn})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_call: //연락처복구

                recover_contact();
                recover_call();
                recover_sms();
                recover_mms();
                dialog();

                break;

            case R.id.btn_file: //파일복구

                recover_file();

                mDbOpenHelper.delete_file_db(); //복구 이후 db 날리기.
                dialog();


                break;
            case R.id.title_icon:
                onBackPressed();
                break;
            case R.id.title_btn:
                //도움말
                break;

        }
    }

    public void dialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage("복구완료");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();


    }



}


//
//    public static Uri insert(Context context, String to, String subject)
//    {
//        try
//        {
//            Uri destUri = Uri.parse("content://mms");
//
//            // Get thread id
//            //Set<String> recipients = new HashSet<String>();
//            //recipients.addAll(Arrays.asList(to));
//            long thread_id = 219;
//            Log.e(">>>>>>>", "Thread ID is " + thread_id);
//
//            // Create a dummy sms
//            ContentValues dummyValues = new ContentValues();
//            dummyValues.put("thread_id", thread_id);
//            dummyValues.put("body", "Dummy SMS body.");
//            Uri dummySms = context.getContentResolver().insert(Uri.parse("content://sms/"), dummyValues);
//
//            // Create a new message entry
//            long now = System.currentTimeMillis();
//            ContentValues mmsValues = new ContentValues();
//            mmsValues.put("thread_id", thread_id);
//            mmsValues.put("date", 1473504346);
////            mmsValues.put("msg_box", Telephony.Mms.MESSAGE_BOX_OUTBOX);
//            mmsValues.put("msg_box", Telephony.Mms.MESSAGE_BOX_INBOX);
//            mmsValues.put("m_id", 1473504346);
//            mmsValues.put("read", 1);
//            mmsValues.put("sub", subject);
//            mmsValues.put("sub_cs", 106);
//            mmsValues.put("ct_t", "application/vnd.wap.multipart.related");
//            //   mmsValues.put("exp", imageBytes.length);
//            mmsValues.put("m_cls", "personal");
//            mmsValues.put("m_type", 128); // 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
//            mmsValues.put("v", 19);
//            mmsValues.put("pri", 129);
//            mmsValues.put("tr_id", "T"+ Long.toHexString(now));
//            mmsValues.put("resp_st", 128);
//
//            // Insert message
//            Uri res = context.getContentResolver().insert(destUri, mmsValues);
//            String messageId = res.getLastPathSegment().trim();
//            Log.e(">>>>>>>", "Message saved as " + res);
//
//            // Create part
//            createPart(context, messageId);
//
//
//            createAddr(context, messageId, to);
//
//
//            //res = Uri.parse(destUri + "/" + messageId);
//
//            // Delete dummy sms
//            context.getContentResolver().delete(dummySms, null, null);
//
//            return res;
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    private static Uri createPart(Context context, String id) throws Exception
//    {
//        ContentValues mmsPartValue = new ContentValues();
//        mmsPartValue.put("mid", id);
//        mmsPartValue.put("ct", "text/plain");
//        mmsPartValue.put("text", "딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가딩가");
//        mmsPartValue.put("cid", "<" + System.currentTimeMillis() + ">");
//        Uri partUri = Uri.parse("content://mms/" + id + "/part");
//        Uri res = context.getContentResolver().insert(partUri, mmsPartValue);
//        Log.e(">>>>>>>", "Part uri is " + res.toString());
//
//        // Add data to part
////        OutputStream os = context.getContentResolver().openOutputStream(res);
////        ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
////        byte[] buffer = new byte[256];
////        for (int len=0; (len=is.read(buffer)) != -1;)
////        {
////            os.write(buffer, 0, len);
////        }
////        os.close();
////        is.close();
//
//        return res;
//    }
//
//    private static Uri createAddr(Context context, String id, String addr) throws Exception
//    {
//        ContentValues addrValues = new ContentValues();
//        addrValues.put("address", addr);
//        addrValues.put("charset", "106");
//        addrValues.put("type", 151); // TO
//        Uri addrUri = Uri.parse("content://mms/"+ id +"/addr");
//        Uri res = context.getContentResolver().insert(addrUri, addrValues);
//        Log.e(">>>>>>>", "Addr uri is " + res.toString());
//
//        return res;
//    }
