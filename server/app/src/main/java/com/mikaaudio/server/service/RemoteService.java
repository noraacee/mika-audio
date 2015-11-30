package com.mikaaudio.server.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.mikaaudio.server.util.CommunicationManager;
import com.mikaaudio.server.util.P2PManager;
import com.mikaaudio.server.util.SuperUserManager;

import java.io.IOException;

public class RemoteService extends Service {

    private CommunicationManager commManager;
    private P2PManager p2pManager;
    private SuperUserManager suManager;

    @Override
    public void onCreate() {
        try {
            suManager = new SuperUserManager();
            commManager = new CommunicationManager(suManager);
            p2pManager = new P2PManager(getApplicationContext(), commManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        commManager.onDestroy();
        p2pManager.onDestroy();
        suManager.onDestroy();
    }
}
