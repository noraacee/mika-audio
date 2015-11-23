package com.mikaaudio.server.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.mikaaudio.server.R;

import com.mikaaudio.server.util.MessageManager;
import com.mikaaudio.server.util.P2PManager;
import com.mikaaudio.server.util.StatusManager;

import java.io.IOException;

public class ServerActivity extends Activity{
    private MessageManager mManager;
    private P2PManager pManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        StatusManager.setStatusView((TextView) findViewById(R.id.status));
        TextView receiveView = (TextView) findViewById(R.id.message_received);

        mManager = new MessageManager(receiveView);

        try {
            pManager = new P2PManager(ServerActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new CmdSocketAcceptTask().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private class CmdSocketAcceptTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... nothing) {
            try {
                pManager.registerCmdService();
                mManager.setCommandSocket(pManager.getCmdSocket().accept());

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
                StatusManager.setStatus("Failed to create connection with socket");
            }
        }
    }

    private class MsgSocketAcceptTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... nothing) {
            try {
                pManager.registerMsgService();
                mManager.setMessageSocket(pManager.getMsgSocket().accept());

                return true;
            } catch (IOException e) {
                e.printStackTrace();

                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mManager.listenCmd();
                mManager.listenMsg();
            } else {
                StatusManager.setStatus("Failed to create connection with socket");
            }
        }
    }
}
