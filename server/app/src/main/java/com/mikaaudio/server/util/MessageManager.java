package com.mikaaudio.server.util;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageManager {
    public static final int CLICK = 0;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;
    public static final int UP = 1;

    private TextView receiveView;

    private InputStream cmdIn;
    private BufferedReader msgIn;

    public MessageManager(TextView receivedView) {
        this.receiveView = receivedView;
    }

    public void listenCmd() {
        new ReadCmdSocketTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void listenMsg() {
        new ReadMsgSocketTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void setCommandSocket(Socket socket) throws IOException {
        Log.d("socket", "command server created at port " + socket.getLocalPort());
        cmdIn = socket.getInputStream();
    }

    public void setMessageSocket(Socket socket) throws IOException {
        Log.d("socket", "message server created at port " + socket.getLocalPort());
        msgIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void parse(int ch) {
        switch(ch) {
            case CLICK:
                receiveView.setText("Click");
                break;
            case UP:
                receiveView.setText("Up");
                break;
            case DOWN:
                receiveView.setText("Down");
                break;
            case LEFT:
                receiveView.setText("Left");
                break;
            case RIGHT:
                receiveView.setText("Right");
                break;
        }
    }

    private void parse(String msg) {
        receiveView.setText(msg);
    }

    private class ReadCmdSocketTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... nothing) {
            try {
                return cmdIn.read();
            } catch (IOException e) {
                Log.e("IOException", e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer ch) {
            if (ch != null) {
                parse(ch);
                listenCmd();
            }
        }
    }

    private class ReadMsgSocketTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... nothing) {
            try {
                return msgIn.readLine();
            } catch (IOException e) {
                Log.e("IOException", e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String msg) {
            if (msg != null) {
                parse(msg);
                listenMsg();
            }
        }
    }
}
