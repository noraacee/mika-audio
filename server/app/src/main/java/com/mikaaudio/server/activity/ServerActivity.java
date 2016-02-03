package com.mikaaudio.server.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mikaaudio.server.R;

import com.mikaaudio.server.service.RemoteService;
import com.mikaaudio.server.util.StatusManager;

public class ServerActivity extends Activity{
    private static final String LIBRARY_SERVER = "server";

    private Button end;
    private Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        StatusManager.getInstance().setStatusView((TextView) findViewById(R.id.status));

        start = (Button) findViewById(R.id.start);
        start.setEnabled(false);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setEnabled(false);
                end.setEnabled(true);

                startService(new Intent(ServerActivity.this, RemoteService.class));

                StatusManager.getInstance().setStatus("service started");
            }
        });

        end = (Button) findViewById(R.id.end);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end.setEnabled(false);
                start.setEnabled(true);

                Intent i = new Intent(ServerActivity.this, RemoteService.class);
                i.putExtra(RemoteService.SERVICE_STOP, true);
                startService(i);

                StatusManager.getInstance().setStatus("service ended");
            }
        });

        startService(new Intent(this, RemoteService.class));
        StatusManager.getInstance().setStatus("service started");

        System.loadLibrary(LIBRARY_SERVER);
    }
}
