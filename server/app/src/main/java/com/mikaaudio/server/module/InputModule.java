package com.mikaaudio.server.module;

import android.app.Instrumentation;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.mikaaudio.server.manager.ModuleManager;
import com.mikaaudio.server.util.Stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputModule {
    private static final int MODE_EXIT = 0x00;
    private static final int MODE_KEY = 0x01;
    private static final int MODE_TEXT = 0x02;

    private static final String TAG = "input";

    private static final InputThread inputThread;

    static {
        inputThread = new InputThread();
    }

    public void inputKeyEvent(int key) {
        synchronized (inputThread) {
            inputThread.inputKeyEvent(key);
        }
    }

    public void inputString(String text) {
        synchronized (inputThread) {
            inputThread.inputString(text);
        }
    }

    public void listen(InputStream in, OutputStream out) {
        byte[] buffer = new byte[Stream.LENGTH_BUFFER];
        ByteArrayOutputStream inString = new ByteArrayOutputStream();

        try {
            out.write(ModuleManager.ACK);
            Log.d("status", "receiving input");

            int mode;
            listening: while (true) {
                mode = in.read();
                switch(mode) {
                    case MODE_EXIT:
                        break listening;
                    case MODE_KEY:
                        inputKeyEvent(in.read());
                        break;
                    case MODE_TEXT:
                        inputString(Stream.readString(in, inString, buffer));
                        break;
                }
            }

            out.write(ModuleManager.ACK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class InputThread extends HandlerThread {
        private Handler inputHandler;
        private Instrumentation inputDispatcher;

        public InputThread() {
            super(TAG);
            inputDispatcher = new Instrumentation();

            start();

            inputHandler = new Handler(getLooper(), initCallback());
        }

        public void inputKeyEvent(int key) {
            inputHandler.obtainMessage(MODE_KEY, key, 0).sendToTarget();
        }

        public void inputString(String text) {
            inputHandler.obtainMessage(MODE_TEXT, text).sendToTarget();
        }

        private Handler.Callback initCallback() {
            return new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what) {
                        case MODE_KEY:
                            Log.d("status", "received key: " + msg.arg1);
                            inputDispatcher.sendKeyDownUpSync(msg.arg1);
                            break;
                        case MODE_TEXT:
                            Log.d("status", "received key: " + msg.obj);
                            inputDispatcher.sendStringSync((String) msg.obj);
                            break;
                    }
                    return true;
                }
            };
        }
    }
}
