package com.hj.nf.myapplication;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by snell1 on 2017-03-07.
 */


//send
public class HeadlessSmsSendService extends IntentService {
    public HeadlessSmsSendService() {
        super(HeadlessSmsSendService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}