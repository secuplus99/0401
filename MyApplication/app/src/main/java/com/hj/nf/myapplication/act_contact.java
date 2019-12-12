package com.hj.nf.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by snell1 on 2017-02-28.
 */
public class act_contact extends Activity {


    @BindView(R.id.title_icon)
    ImageView titleIcon;
    @BindView(R.id.title_btn)
    ImageView titleBtn;
    private MySQLiteOpenHelper mDbOpenHelper;


    String phoneNumber;
    String name;
    ArrayList<Contact> aa = new ArrayList<Contact>();
    ArrayList<Contact> selectedList = new ArrayList<Contact>();
    ContactAdapter3 contactAdapter;

    @BindView(R.id.backbtn)
    Button backbtn;
    @BindView(R.id.nextbtn)
    Button nextbtn;
    @BindView(R.id.rv)
    RecyclerView rv;
    @BindView(R.id.search_text)
    EditText searchText;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_contact);

        ButterKnife.bind(this);
        contactAdapter = new ContactAdapter3(aa);  //list 어댑터
        rv.setAdapter(contactAdapter); // RecyclerView setAdapter

        mDbOpenHelper = new MySQLiteOpenHelper(this); //db열기
        mDbOpenHelper.open();

        getNumber(this.getContentResolver());  // 연락처 목록 띄우기


        // text가 바뀔때마다 검색결과 나타내기
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                //필터링 검색결과
                String text = searchText.getText().toString().toLowerCase(Locale.getDefault());
                contactAdapter.getFilter().filter(text);
            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }
        });

    }

    @Override
    protected void onDestroy() {super.onDestroy();mDbOpenHelper.close();}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    // 연락처를 숫자 > 가나다 순으로 이름 정렬
    private final static Comparator<Contact> myComparator = new Comparator<Contact>() {

        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(Contact object1, Contact object2) {
            return collator.compare(object1.name, object2.name);

        }
    };


    //todo 연락처 띄우기
    public void getNumber(ContentResolver cr) {
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            //이름 번호를 uri를 통해서 받고
            name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            //Contact 형식으로 리스트에 넣기
            aa.add(new Contact(name, phoneNumber));


        }
        phones.close();// close cursor

        // 어댑터의 리스트들을 이름순으로 정렬
        Collections.sort(aa, myComparator);
        //갱신
        contactAdapter.notifyDataSetChanged();

    }

    //todo 체크박스에 선택 된 리스트들을 가져오기
    private void getSelectedList() {
        selectedList.clear();
        for (int i = 0; i < aa.size(); i++) {
            if (aa.get(i).isCheck) {
                selectedList.add(aa.get(i));
            }
        }
        //체크된 리스트들 받아서 할일
        mDbOpenHelper.delete_sms_mms_contact();
        getDatas(); //선택된 연락처를 통해서 DB작업 수행
    }


    //todo 선택된 리스트들 연락처.문자. data 받고 db에 넣기

    private void getDatas() {

    // TODO: 2017-03-28 contact db에 정보 입력

        for (int i = 0; i < selectedList.size(); i++) {

            String num1 = selectedList.get(i).phone.replaceAll("-", "");  //번호
            String name1 = selectedList.get(i).name;                    //이름

            mDbOpenHelper.insert_number(name1, num1); //db에 정보 입력
            Log.d("이름 번호", "" + num1 + name1);

        }


//// TODO: 2017-03-06  sms db에 정보 입력

        Uri allMessage = Uri.parse("content://sms");
        ContentResolver sms = getContentResolver();

        Cursor c = sms.query(allMessage,
                new String[]{"_id", "thread_id", "address", "person", "date", "body",
                        "protocol", "read", "status", "type", "reply_path_present",
                        "subject", "service_center", "locked", "error_code", "seen"},
                null, null,
                "date DESC");

        while (c.moveToNext()) {

            String find_smsnum = c.getString(2);

            for (int i = 0; i < selectedList.size(); i++) {

                //선택한 전번이랑 문자중에 그 전번 문자일경우
                if (selectedList.get(i).phone.replaceAll("-", "").equals(find_smsnum)) {


                    ///////    SMS DB 인덱스 번호  -  위의 query 내 String array 내용과 맵핑

                    int threadId = c.getInt(1);
                    String address = c.getString(2); //번호
                    String Name = c.getString(3);   //이름
                    String Date = c.getString(4);   //시간
                    String Body = c.getString(5);   //내용
                    String Type = c.getString(9);   //수,발신 타입
                    int read = c.getInt(7);         //읽음 , 안읽음

                    //db에 정보 입력
                    mDbOpenHelper.insert_sms(address, Name, Date, Body, Type, threadId, read);

                }


            }
        }
        c.close();


        //// TODO: 2017-03-20 mms db에 정보입력


        for (int i = 0; i < selectedList.size(); i++) {

            //선택한 전번이랑 문자중에 그 전번 문자일경우

            // getThread1 함수를 통해서 thread id를 얻는다
            // 이때 이 번호에 해당하는 문자가 1개이상 있어야 thread_id를 얻을 수 있습니다. mms 정보를 여는데 thread_id가 필요합니다

            String selected_thread = getThread1(selectedList.get(i).phone.replaceAll("-", ""));
            Uri mms_uri = Uri.parse("content://mms-sms/conversations/" + selected_thread);

            try {
                Cursor mms = getContentResolver().query(mms_uri, new String[]{"thread_id", "_id", "date", "m_type", "error_code", "ct_t", "_data", "m_type"}, null, null, null);

                while (mms.moveToNext()) {

                    String selectionPart = "mid=" + mms.getString(1);
                    Uri uri = Uri.parse("content://mms/part");
                    Cursor cursor = getContentResolver().query(uri, null, selectionPart, null, null);

                    if (cursor.moveToFirst()) {
                        do {
                            String partId = cursor.getString(cursor.getColumnIndex("_id"));
                            String type = cursor.getString(cursor.getColumnIndex("ct"));
                            //mms형식이 text/ plain인 경우
                            if ("text/plain".equals(type)) {
                                String data = cursor.getString(cursor.getColumnIndex("_data"));
                                String body;
                                if (data != null) {
                                    // data가 있는경우 getMmsText로 뽑습니다
                                    body = getMmsText(partId);
                                    Log.d("mms1", "" + body);
                                }
                                else {
                                    //보통 이 구문으로 mms text를 뽑습니다.
                                    body = cursor.getString(cursor.getColumnIndex("text"));
                                    Log.d("mms2", "" + body);

                                    mDbOpenHelper.insert_mms(selectedList.get(i).phone.replaceAll("-", ""), mms.getString(2), body, mms.getString(3), mms.getInt(0));
                                    //전번 , 시간 , 내용 , 타입 , 스레드id
                                }

                            }
                        } while (cursor.moveToNext());
                    }

                }
                mms.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }


        // TODO: 2017-03-06 call log 통화기록
        Cursor[] cursors = new Cursor[2];
        Uri CallsUri = CallLog.Calls.CONTENT_URI;
        //퍼미션 체크
        if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            cursors[0] = getContentResolver().query(CallsUri, null, null, null, null);
        }

        Cursor callsCursor = cursors[0];


        while (callsCursor.moveToNext()) {

            //콜커서(모든 콜로그)가 가지는 번호
            String find_call_log_num = callsCursor.getString(callsCursor.getColumnIndex(CallLog.Calls.NUMBER));

            for (int i = 0; i < selectedList.size(); i++) {

                //콜커서 번호와 선택된 번호가 맞을경우
                if (selectedList.get(i).phone.replaceAll("-", "").equals(find_call_log_num)) {


                    int number1 = callsCursor.getColumnIndex(CallLog.Calls.NUMBER); //번호
                    int name1 = callsCursor.getColumnIndex(CallLog.Calls.CACHED_NAME); //이름
                    int date1 = callsCursor.getColumnIndex(CallLog.Calls.DATE);   //시간
                    int duration1 = callsCursor.getColumnIndex(CallLog.Calls.DURATION); //통화시간
                    int type1 = callsCursor.getColumnIndex(CallLog.Calls.TYPE); // 수,발신,부재중 타입


                    String phNum = callsCursor.getString(number1);
                    String phName = callsCursor.getString(name1);
                    String strcallDate = callsCursor.getString(date1);
                    String strdur = callsCursor.getString(duration1);
                    String callTypeCode = callsCursor.getString(type1);

                    mDbOpenHelper.insert_calllog(phNum, phName, strcallDate, strdur, callTypeCode); //db에 추가


                }

            }
        }
        callsCursor.close();// close cursor


        //// TODO: 2017-03-08  DB 저장 이후 삭제

        //todo 연락처 삭제
        Cursor delete_contact_number = mDbOpenHelper.getNumbercolumns();
        while (delete_contact_number.moveToNext()) {
            DeleteContact("0" + delete_contact_number.getString(delete_contact_number.getColumnIndex("n_num")),
                    delete_contact_number.getString(delete_contact_number.getColumnIndex("n_name")));
        }

        //todo 통화기록 삭제
        Cursor delete_call_Cursor = mDbOpenHelper.getCallcolumns();
        while (delete_call_Cursor.moveToNext()) {
            DeleteCallLog(delete_call_Cursor.getString(delete_call_Cursor.getColumnIndex("c_num")));
        }

        //todo sms mms 삭제
        Cursor delete_sms_Cursor;
        delete_sms_Cursor = mDbOpenHelper.getSmscolumns();
        while (delete_sms_Cursor.moveToNext()) {

            String thread__id = "" + delete_sms_Cursor.getInt(delete_sms_Cursor.getColumnIndex("s_thread_id"));

            //thread id를 가지고 삭제
            Uri sms_inboxUri = Uri.parse("content://sms/");
            getContentResolver().delete(sms_inboxUri, Telephony.Sms.THREAD_ID + "=?", new String[]{thread__id});

            Uri mms_inboxUri = Uri.parse("content://mms/");
            getContentResolver().delete(mms_inboxUri, Telephony.Mms.THREAD_ID + "=?", new String[]{thread__id});

        }
    }


    private String getMmsText(String id) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }


    private String getThread1(String addr) {

        try {
            //sms에 찾으려는 번호에 해당하는 thread_id를 cursor로 얻는다.
            Cursor c = getContentResolver().query(Uri.parse("content://sms"), new String[]{"thread_id"},
                    "address" + " = ?",
                    new String[]{addr}, "date DESC");

            //date desc로 가장 최근 thread_id로
            c.moveToFirst();
            String thread_id = c.getString(0);
            c.close();

            return thread_id;

        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }



    public void DeleteCallLog(String number) {
        String queryString = "0" + number;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        this.getContentResolver().delete(CallLog.Calls.CONTENT_URI, queryString, null);

    }

    public boolean DeleteContact(String phone, String name) {
        // 연락처 uri
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        //모든 연락처를 얻고
        Cursor cur = getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cur.moveToFirst()) {
                do {
                    //같을경우
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        //삭제
                        getContentResolver().delete(uri, null, null);
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


    @OnClick({R.id.backbtn, R.id.nextbtn, R.id.title_icon, R.id.title_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backbtn: onBackPressed();
                break;
            case R.id.nextbtn: // 선택 끝난 이후 > 버튼

                getSelectedList();
                dialog();

                break;
            case R.id.title_icon: onBackPressed();
                break;
            case R.id.title_btn:

                //도움말
                break;


        }
    }

    public void dialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage("기기에서 문자 및 통화기록이 숨겨집니다.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();


    }

}