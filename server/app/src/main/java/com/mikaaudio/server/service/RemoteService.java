package com.mikaaudio.server.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.mikaaudio.server.R;
import com.mikaaudio.server.activity.ServerActivity;
import com.mikaaudio.server.util.CommunicationManager;
import com.mikaaudio.server.util.FrameManager;
import com.mikaaudio.server.util.P2PManager;

import java.io.IOException;

public class RemoteService extends Service {
    public static final String SERVICE_STOP = "stop";

    // Cannot be 0
    private static final int NOTIFICATION_ID = 1;

    private static final String NOTIFICATION_TEXT = "Mika Audio";
    private static final String NOTIFICATION_TITLE = "Remote control Server running";

    private boolean running;

    private CommunicationManager commManager;
    private FrameManager frameManager;
    private P2PManager p2pManager;
    private SuperUserManager suManager;

    public RemoteService() {
        super();
    }

    @Override
    public void onCreate() {
        running = false;
        Log.d("status", "created");
        try {
            frameManager = new FrameManager();
            suManager = new SuperUserManager();
            commManager = new CommunicationManager(suManager);
            p2pManager = new P2PManager(getApplicationContext(), commManager, frameManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(SERVICE_STOP)) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (running) {
            Log.d("status", "service already running");
            return START_NOT_STICKY;
        }

        running = true;

        try {
            p2pManager.registerService();
        } catch (IOException e) {
            running = false;
            e.printStackTrace();
            return START_STICKY;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentTitle(NOTIFICATION_TITLE);
        notificationBuilder.setContentText(NOTIFICATION_TEXT);

        Intent serverIntent = new Intent(this, ServerActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ServerActivity.class);
        stackBuilder.addNextIntent(serverIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());

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
        frameManager.onDestroy();
        p2pManager.onDestroy();
        suManager.onDestroy();
    }
}
