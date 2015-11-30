package com.mikaaudio.server.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class CommunicationManager {
    public enum Mode {
        RUNNING, STOPPED
    }

    private Mode mode;

    private BufferedReader msgIn;
    private InputStream keyIn;
    private SuperUserManager suManager;

    private ReadKeySocketTask kTask;
    private ReadMsgSocketTask mTask;

    public CommunicationManager(SuperUserManager suManager) {
        this.suManager = suManager;
        mode = Mode.STOPPED;
    }

    public void listenKey() {
        if (mode == Mode.RUNNING) {
            kTask = new ReadKeySocketTask();
            kTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void listenMsg() {
        if (mode == Mode.RUNNING) {
            mTask = new ReadMsgSocketTask();
            mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void onDestroy() {
        mode = Mode.STOPPED;
        kTask.cancel(true);
        mTask.cancel(true);
    }

    public void setKeySocket(Socket socket) throws IOException {
        Log.d("socket", "command server created at port " + socket.getLocalPort());
        keyIn = socket.getInputStream();
    }

    public void setMessageSocket(Socket socket) throws IOException {
        Log.d("socket", "message server created at port " + socket.getLocalPort());
        msgIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    private void parse(int key) {
        Log.d("parsing", Integer.toString(key));
        try {
            suManager.inputKeyevent(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parse(String msg) {
        Log.d("parsing", msg);
    }

    private class ReadKeySocketTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... nothing) {
            try {
                return keyIn.read();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer key) {
            if (key != -1) {
                parse(key);
                listenKey();
            } else {
                
            }
        }
    }

    private class ReadMsgSocketTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... nothing) {
            try {
                return msgIn.readLine();
            } catch (IOException e) {
                e.printStackTrace();
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
