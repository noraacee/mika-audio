package com.mikaaudio.server.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mikaaudio.server.util.CommunicationManager;
import com.mikaaudio.server.util.P2PManager;
import com.mikaaudio.server.util.SuperUserManager;

import java.io.IOException;

public class RemoteService extends Service {
    private boolean running;

    private CommunicationManager cManager;
    private P2PManager pManager;
    private SuperUserManager suManager;

    @Override
    public void onCreate() {
        running = false;

        try {
            suManager = new SuperUserManager();
            cManager = new CommunicationManager(suManager);
            pManager = new P2PManager(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (running)
            return START_REDELIVER_INTENT;

        running = true;

        new KeySocketAcceptTask().execute();

        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        cManager.onDestroy();
        pManager.onDestroy();
        suManager.onDestroy();
    }

    private class KeySocketAcceptTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... nothing) {
            try {
                pManager.registerKeyService();
                cManager.setKeySocket(pManager.getKeySocket().accept());

                return true;
            } catch (IOException e) {
                e.printStackTrace();

                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                new MsgSocketAcceptTask().execute();
            } else {
                running = false;
                Log.e("status", "Failed to create connection with key socket");
            }
        }
    }

    private class MsgSocketAcceptTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... nothing) {
            try {
                pManager.registerMsgService();
                cManager.setMessageSocket(pManager.getMsgSocket().accept());

                return true;
            } catch (IOException e) {
                e.printStackTrace();

                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                cManager.setMode(CommunicationManager.Mode.RUNNING);
                cManager.listenKey();
                cManager.listenMsg();
            } else {
                running = false;
                Log.e("status", "Failed to create connection with message socket");
            }
        }
    }
}
