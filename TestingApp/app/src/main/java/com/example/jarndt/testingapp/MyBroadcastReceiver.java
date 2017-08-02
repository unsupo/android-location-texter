package com.example.jarndt.testingapp;

import android.content.BroadcastReceiver;

/**
 * Created by jarndt on 7/29/17.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context aContext, Intent aIntent) {

        // This is where you start your service
        aContext.startService(new Intent(aContext, MyService.class));
    }
}