package com.mikaaudio.client.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mikaaudio.client.R;
import com.mikaaudio.client.util.CommunicationManager;
import com.mikaaudio.client.util.P2PManager;
import com.mikaaudio.client.util.StatusManager;

import java.io.IOException;
import java.net.Socket;

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

    private P2PManager pManager;
    private CommunicationManager cManager;

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
                cManager.write(sendView.getText().toString());
            }
        });

        click = (Button) findViewById(R.id.click);
        click.setEnabled(false);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cManager.write(CommunicationManager.KEY_CLICK);
            }
        });

        back = (Button) findViewById(R.id.back);
        back.setEnabled(false);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cManager.write(CommunicationManager.KEY_BACK);
            }
        });

        home = (Button) findViewById(R.id.home);
        home.setEnabled(false);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cManager.write(CommunicationManager.KEY_HOME);
            }
        });

        apps = (Button) findViewById(R.id.apps);
        apps.setEnabled(false);
        apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cManager.write(CommunicationManager.KEY_APPS);
            }
        });

        up = (Button) findViewById(R.id.up);
        up.setEnabled(false);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cManager.write(CommunicationManager.KEY_UP);
            }
        });

        down = (Button) findViewById(R.id.down);
        down.setEnabled(false);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cManager.write(CommunicationManager.KEY_DOWN);
            }
        });

        left = (Button) findViewById(R.id.left);
        left.setEnabled(false);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cManager.write(CommunicationManager.KEY_LEFT);
            }
        });

        right = (Button) findViewById(R.id.right);
        right.setEnabled(false);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cManager.write(CommunicationManager.KEY_RIGHT);
            }
        });

        pManager = new P2PManager(this);
        cManager = new CommunicationManager();

        new ConnectServerTask().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void setKeySocket(Socket socket) {
        try {
            cManager.setKeySocket(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMsgSocket(Socket socket) {
        try {
            cManager.setMessageSocket(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectServerTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... nothing) {
            pManager.connect();
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
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
    }
}
