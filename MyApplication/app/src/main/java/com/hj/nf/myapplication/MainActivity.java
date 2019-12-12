package com.hj.nf.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @BindView(R.id.btn_call)
    Button btnCall;
    @BindView(R.id.btn_file)
    Button btnFile;
    @BindView(R.id.btn_recover)
    Button btnRecover;
    @BindView(R.id.txt_recover)
    TextView txtRecover;

    @BindView(R.id.switch1)
    Switch switch1;
    @BindView(R.id.txt_title)
    TextView txtTitle;

    private MySQLiteOpenHelper mDbOpenHelper;


    //private RemoteControlReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        ButterKnife.bind(this);
        mDbOpenHelper = new MySQLiteOpenHelper(this);


        // 오디오 이벤트 서비스 등록
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.registerMediaButtonEventReceiver(new ComponentName("com.hj.nf.myapplication", RemoteControlReceiver.class.getName()));


        // TODO: 2017-02-17 날릴때 쓴다 오디오 이벤트 서비스 해제
        //am.unregisterMediaButtonEventReceiver(new ComponentName("com.hj.nf.myapplication", RemoteControlReceiver.class.getName()));


        // 문자 디폴트 앱 설정 구문
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();
            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {

                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
                startActivityForResult(intent, 1);

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDbOpenHelper.open();

        //파일과 연락처 DB열기
        Cursor mCursor = mDbOpenHelper.getNumbercolumns();
        Cursor mmCursor = mDbOpenHelper.getFilecolumns();
        //   try {
        //파일과 연락처에 설정된 DB가 둘다 없다면 초기 환경설정
        mCursor.moveToFirst();
        mmCursor.moveToFirst();

        if (mCursor.getCount() == 0 && mmCursor.getCount() == 0) {
            txtTitle.setText("초기 환경설정");
            txtRecover.setVisibility(View.INVISIBLE);
            btnRecover.setVisibility(View.INVISIBLE);
        }
        //     }
        //    catch (e)
        else {
            txtTitle.setText("환경설정");
            txtRecover.setVisibility(View.VISIBLE);
            btnRecover.setVisibility(View.VISIBLE);
        }
        mDbOpenHelper.close();

        // 아이콘 활성화, 비활성화 나타내기
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        String key = mPref.getString("show", "on");

        if (key.equals("off"))
            switch1.setChecked(true);
        else
            switch1.setChecked(false);
    }


    @OnClick({R.id.btn_call, R.id.btn_file, R.id.btn_recover, R.id.switch1})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_call: //연락처
                Intent intent = new Intent(this, act_contact.class);
                startActivity(intent);

                break;
            case R.id.btn_file: //파일

                Intent intent3 = new Intent(this, act_file_chooser.class);
                startActivity(intent3);


                break;
            case R.id.btn_recover: //복구
                Intent intent2 = new Intent(this, act_recover.class);
                startActivity(intent2);
                break;

            case R.id.switch1:  //아이콘 숨기기, 나타내기 스위치
                SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
                PackageManager p = getPackageManager();
                ComponentName componentName = new ComponentName(this, act_start.class);
                SharedPreferences.Editor editor = mPref.edit();

                String key = mPref.getString("show", "on");

                if (key.equals("off")) {
                    editor.putString("show", "on");
                    editor.commit();
                    p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    Toast toast1 = Toast.makeText(this, "아이콘 비활성화", Toast.LENGTH_SHORT);
                    toast1.show();


                } else {
                    editor.putString("show", "off");
                    editor.commit();
                    p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    Toast toast1 = Toast.makeText(this, "아이콘 활성화", Toast.LENGTH_SHORT);
                    toast1.show();
                }
                break;
        }
    }

}
