package com.mikaaudio.server.module;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.mikaaudio.server.manager.ModuleManager;
import com.mikaaudio.server.util.ByteUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class InputModule {
    private static final int MODE_EXIT = 0x00;
    private static final int MODE_KEY = 0x01;
    private static final int MODE_TEXT = 0x02;
    private static final int MODE_EVENT = 0x03;

    private float screenHeight;
    private float screenWidth;

    private static final String TAG = "INPUT";

    private static final InputThread inputThread;

    static {
        inputThread = new InputThread();
    }

    public InputModule(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    public void inputKeyEvent(int key) {
        synchronized (inputThread) {
            inputThread.inputKeyEvent(key);
        }
    }

    public void inputMotionEvent(MotionEvent ev) {
        synchronized (inputThread) {
            inputThread.inputMotionEvent(ev);
        }
    }

    public void inputString(String text) {
        synchronized (inputThread) {
            inputThread.inputString(text);
        }
    }

    public void listen(InputStream in, OutputStream out) {
        byte[] buffer = new byte[ByteUtil.LENGTH_BUFFER];
        ByteArrayOutputStream inString = new ByteArrayOutputStream();

        try {
            out.write(ModuleManager.ACK);
            Log.d(TAG, "receiving input");

            int mode;
            listening: while (true) {
                mode = in.read();
                switch(mode) {
                    case MODE_EXIT:
                        Log.d(TAG, "exiting");
                        break listening;
                    case MODE_KEY:
                        inputKeyEvent(in.read());
                        break;
                    case MODE_TEXT:
                        inputString(ByteUtil.readString(in, inString, buffer));
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

        public void inputMotionEvent(MotionEvent ev) {
            inputHandler.obtainMessage(MODE_EVENT, ev).sendToTarget();
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
                            inputDispatcher.sendKeyDownUpSync(msg.arg1);
                            break;
                        case MODE_TEXT:
                            inputDispatcher.sendStringSync((String) msg.obj);
                            break;
                        case MODE_EVENT:
                            inputDispatcher.sendPointerSync((MotionEvent) msg.obj);
                    }
                    return true;
                }
            };
        }
    }
}
