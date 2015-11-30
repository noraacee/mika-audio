package com.mikaaudio.server.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mikaaudio.server.R;

import com.mikaaudio.server.service.RemoteService;
import com.mikaaudio.server.util.StatusManager;

public class ServerActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        StatusManager.getInstance().setStatusView((TextView) findViewById(R.id.status));

        findViewById(R.id.end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(ServerActivity.this, RemoteService.class));
            }
        });

        startService(new Intent(this, RemoteService.class));
    }
}
