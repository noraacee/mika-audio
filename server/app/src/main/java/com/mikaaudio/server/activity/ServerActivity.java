package com.mikaaudio.server.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mikaaudio.server.R;

import com.mikaaudio.server.util.P2PManager;
import com.mikaaudio.server.util.StatusManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerActivity extends Activity{
    private TextView receiveView;
    private EditText sendView;
    private Button sendButton;

    private BufferedReader in;
    private PrintWriter out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        StatusManager.setStatusView((TextView) findViewById(R.id.status));

        receiveView = (TextView) findViewById(R.id.message_received);
        sendView = (EditText) findViewById(R.id.message_to_send);
        sendButton = (Button) findViewById(R.id.send);

        sendView.setEnabled(false);
        sendButton.setEnabled(false);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                out.println(sendView.getText().toString());
            }
        });


        new SocketAcceptTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void appendMessage(String message) {
        receiveView.append(message);
    }

    private void listenForMessage() {
        new ReadSocketTask().execute();
    }

    private class SocketAcceptTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                P2PManager p2pManager = new P2PManager(ServerActivity.this);

                Socket socket = p2pManager.getSocket().accept();

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                return true;
            } catch (IOException e) {
                Log.e("IOException", e.toString());

                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                sendView.setEnabled(true);
                sendButton.setEnabled(true);

                listenForMessage();
            } else {
                StatusManager.setStatus("Failed to create connection with socket");
            }
        }
    }

    private class ReadSocketTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                return in.readLine();
            } catch (IOException e) {
                Log.e("IOException", e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String message) {
            if (message != null) {
                appendMessage(message);
                listenForMessage();
            }
        }
    }
}
