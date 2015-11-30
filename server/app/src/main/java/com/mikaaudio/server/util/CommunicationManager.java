package com.mikaaudio.server.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class CommunicationManager {
    public enum Mode {
        RUNNING, STOPPED
    }

    private static final int ACTION_CLICK = 23;
    private static final int ACTION_BACK = 4;
    private static final int ACTION_HOME = 3;
    private static final int ACTION_APPS = 187;
    private static final int ACTION_UP = 19;
    private static final int ACTION_DOWN = 20;
    private static final int ACTION_LEFT = 21;
    private static final int ACTION_RIGHT = 22;

    private static final int[] ACTIONS = {ACTION_CLICK, ACTION_BACK, ACTION_HOME, ACTION_APPS, ACTION_UP, ACTION_DOWN, ACTION_LEFT, ACTION_RIGHT};

    private static final String COMMAND_INPUT_KEYEVENT = "input keyevent ";

    /*
    public static final int KEY_CLICK = 0;
    public static final int KEY_BACK = 1;
    public static final int KEY_HOME = 2;
    public static final int KEY_APPS = 3;
    public static final int KEY_UP = 4;
    public static final int KEY_DOWN = 5;
    public static final int KEY_LEFT = 6;
    public static final int KEY_RIGHT = 7;
    */

    private Mode mode;

    private BufferedReader msgIn;
    private DataOutputStream shell;
    private InputStream keyIn;

    public CommunicationManager(DataOutputStream shell) {
        this.shell = shell;
        mode = Mode.STOPPED;
    }

    public void listenKey() {
        if (mode == Mode.RUNNING)
            new ReadKeySocketTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void listenMsg() {
        if (mode == Mode.RUNNING)
            new ReadMsgSocketTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            shell.writeBytes(COMMAND_INPUT_KEYEVENT + key + "\n");
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
            if (key != null) {
                parse(key);
                listenKey();
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
