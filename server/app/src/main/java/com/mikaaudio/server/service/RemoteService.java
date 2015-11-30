package com.mikaaudio.server.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.mikaaudio.server.util.CommunicationManager;
import com.mikaaudio.server.util.P2PManager;

import java.io.DataOutputStream;
import java.io.IOException;

public class RemoteService extends Service {
    private static final String SUPER_USER = "su";

    private DataOutputStream shell;
    private Process su;

    private CommunicationManager cManager;
    private P2PManager pManager;

    @Override
    public void onCreate() {
        try {
            su = Runtime.getRuntime().exec(SUPER_USER);
            shell = new DataOutputStream(su.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            pManager = new P2PManager(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
