package com.mikaaudio.server.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mikaaudio.server.util.CommunicationManager;
import com.mikaaudio.server.util.P2PManager;
import com.mikaaudio.server.util.SuperUserManager;

import java.io.IOException;

public class RemoteService extends Service {
    private static boolean running;

    private CommunicationManager commManager;
    private P2PManager p2pManager;
    private SuperUserManager suManager;

    @Override
    public void onCreate() {
        running = false;

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
        if (running) {
            Log.d("status", "Service already running");
            return START_REDELIVER_INTENT;
        }

        Log.d("status", "Starting service");
        running = true;

        try {
            p2pManager.registerService();
        } catch (IOException e) {
            running = false;
            e.printStackTrace();
        }

        return START_REDELIVER_INTENT;
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
