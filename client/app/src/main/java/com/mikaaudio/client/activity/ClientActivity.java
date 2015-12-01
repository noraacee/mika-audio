package com.mikaaudio.client.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mikaaudio.client.R;
import com.mikaaudio.client.interf.OnConnectListener;
import com.mikaaudio.client.interf.OnDisconnectListener;
import com.mikaaudio.client.util.CommunicationManager;
import com.mikaaudio.client.util.P2PManager;
import com.mikaaudio.client.util.StatusManager;

public class ClientActivity extends Activity {
    private Button apps;
    private Button back;
    private Button click;
    private Button down;
    private Button home;
    private Button left;
    private Button right;
    private Button send;
    private Button up;
    private EditText sendView;

    private P2PManager p2pManager;
    private CommunicationManager commManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        StatusManager.getInstance().setStatusView((TextView) findViewById(R.id.status));
        sendView = (EditText) findViewById(R.id.message_to_send);
        sendView.setEnabled(false);

        send = (Button) findViewById(R.id.send);
        send.setEnabled(false);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commManager.write(sendView.getText().toString());
            }
        });

        click = (Button) findViewById(R.id.click);
        click.setEnabled(false);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commManager.write(CommunicationManager.KEY_CLICK);
            }
        });

        back = (Button) findViewById(R.id.back);
        back.setEnabled(false);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commManager.write(CommunicationManager.KEY_BACK);
            }
        });

        home = (Button) findViewById(R.id.home);
        home.setEnabled(false);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commManager.write(CommunicationManager.KEY_HOME);
            }
        });

        apps = (Button) findViewById(R.id.apps);
        apps.setEnabled(false);
        apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commManager.write(CommunicationManager.KEY_APPS);
            }
        });

        up = (Button) findViewById(R.id.up);
        up.setEnabled(false);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commManager.write(CommunicationManager.KEY_UP);
            }
        });

        down = (Button) findViewById(R.id.down);
        down.setEnabled(false);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commManager.write(CommunicationManager.KEY_DOWN);
            }
        });

        left = (Button) findViewById(R.id.left);
        left.setEnabled(false);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commManager.write(CommunicationManager.KEY_LEFT);
            }
        });

        right = (Button) findViewById(R.id.right);
        right.setEnabled(false);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commManager.write(CommunicationManager.KEY_RIGHT);
            }
        });

        commManager = new CommunicationManager(new OnDisconnectListener() {
            @Override
            public void onDisconnect() {
                p2pManager.connect();
            }
        });
        p2pManager = new P2PManager(this, commManager, new OnConnectListener() {
            @Override
            public void onConnect() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        send.setEnabled(true);
                        click.setEnabled(true);
                        back.setEnabled(true);
                        home.setEnabled(true);
                        apps.setEnabled(true);
                        up.setEnabled(true);
                        down.setEnabled(true);
                        left.setEnabled(true);
                        right.setEnabled(true);
                        sendView.setEnabled(true);
                    }
                });
            }
        });

        p2pManager.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        p2pManager.onDestroy();
        commManager.onDestroy();
    }
}
