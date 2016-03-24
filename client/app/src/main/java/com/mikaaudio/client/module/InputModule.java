package com.mikaaudio.client.module;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.io.OutputStream;

public class InputModule {
    public static final int KEY_APPS = KeyEvent.KEYCODE_APP_SWITCH;
    public static final int KEY_BACK = KeyEvent.KEYCODE_BACK;
    public static final int KEY_CLICK = KeyEvent.KEYCODE_DPAD_CENTER;
    public static final int KEY_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
    public static final int KEY_HOME = KeyEvent.KEYCODE_HOME;
    public static final int KEY_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
    public static final int KEY_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
    public static final int KEY_UP = KeyEvent.KEYCODE_DPAD_UP;

    private static final int MODE_EXIT = 0x00;
    private static final int MODE_KEY = 0x01;
    private static final int MODE_TEXT = 0x02;

    private static final String CHARSET_UTF_8 = "UTF-8";
    private static final String TAG = "input";

    private InputThread inputThread;

    public InputModule(OutputStream out) {
        inputThread = new InputThread(out);
    }

    public void exit() {
        inputThread.exit();
    }

    public void onDestroy() {
        exit();
        inputThread.quit();
    }

    public void sendInput(int key) {
        inputThread.sendInput(key);
    }

    public void sendInput(String text) {
        inputThread.sendInput(text);
    }

    private static class InputThread extends HandlerThread {

        private Handler inputHandler;
        private OutputStream out;

        public InputThread(OutputStream out) {
            super(TAG);
            this.out = out;

            start();

            inputHandler = new Handler(getLooper(), initCallback());
        }

        public void exit() {
            inputHandler.obtainMessage(MODE_EXIT).sendToTarget();
        }

        public void sendInput(int key) {
            inputHandler.obtainMessage(MODE_KEY, key, 0).sendToTarget();
        }

        public void sendInput(String text) {
            inputHandler.obtainMessage(MODE_TEXT, text).sendToTarget();
        }

        private Handler.Callback initCallback() {
            return new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    try {
                        switch (msg.what) {
                            case MODE_EXIT:
                                Log.d("status", "exitting input module");
                                out.write(MODE_EXIT);
                                break;
                            case MODE_KEY:
                                Log.d("status", "sending key: " + msg.arg1);
                                out.write(MODE_KEY);
                                out.write(msg.arg1);
                                break;
                            case MODE_TEXT:
                                Log.d("status", "sending text: " + msg.obj);
                                out.write(MODE_TEXT);
                                out.write(((String) msg.obj).getBytes(CHARSET_UTF_8));
                                break;
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                    }

                    return true;
                }
            };
        }
    }
}
