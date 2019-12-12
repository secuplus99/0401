package com.hj.nf.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class act_start extends Activity {

    @BindView(R.id.start_btn)
    ImageView startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
            }
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            }
        };

        ///추가된 라이브러리 - TedPermission ------------->> build.gradle   ->    compile 'gun0912.ted:tedpermission:1.0.3' 추가
        ///권한 승인 라이브러리 입니다. 마시멜로우 버전 이후로는 자동 권한승인이 되지 않습니다.
        new TedPermission(this).setPermissionListener(permissionListener).setDeniedMessage("권한을 허용지 않으면 기능을 이용할 수 없습니다.")
                .setPermissions(Manifest.permission.READ_SMS,
                        Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG)
                .check();

/////// 1초후 자동실행 핸들러
//        Handler handler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                finish();
//                startActivity(new Intent(act_start.this, MainActivity.class));
//            }
//        };
//        handler.sendEmptyMessageDelayed(0,1000);


    }


    @OnClick(R.id.start_btn)
    public void onClick() {
        startActivity(new Intent(act_start.this, MainActivity.class));
    }
}
