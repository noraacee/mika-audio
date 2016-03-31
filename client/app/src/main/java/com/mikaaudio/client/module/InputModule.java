package com.mikaaudio.client.module;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.mikaaudio.client.util.ByteUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
    private static final int MODE_EVENT = 0x03;

    private static final int POSITION_ACTION = 0;
    private static final int POSITION_X = 4;
    private static final int POSITION_Y = 8;
    private static final int POSITION_META_STATE = 12;

    private static final int SIZE_PACKET = 16;

    private static final String CHARSET_UTF_8 = "UTF-8";
    private static final String TAG = "input";

    private InputThread inputThread;

    public InputModule(OutputStream out) {
        inputThread = new InputThread(out);
    }

    public void exit() {
        inputThread.exit();
    }

    public void initFrameInput(InetAddress ip, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket(0);
        DatagramPacket packet = new DatagramPacket(new byte[SIZE_PACKET], SIZE_PACKET, ip, port);

        inputThread.initFrameInput(socket, packet);
    }

    public void onDestroy() {
        exit();
        inputThread.quit();
    }

    public void sendInput(int key) {
        inputThread.sendInput(key);
    }

    public void sendInput(int action, float x, float y, int metaState) {
        inputThread.sendInput(action, x, y, metaState);
    }

    public void sendInput(String text) {
        inputThread.sendInput(text);
    }

    private static class InputThread extends HandlerThread {
        private byte[] data;

        private DatagramPacket packet;
        private DatagramSocket socket;
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

        public void initFrameInput(DatagramSocket socket, DatagramPacket packet) {
            this.socket = socket;
            this.packet = packet;

            data = packet.getData();
        }

        public void sendInput(int key) {
            inputHandler.obtainMessage(MODE_KEY, key, 0).sendToTarget();
        }

        public void sendInput(int action, float x, float y, int metaState) {
            ByteUtil.writeInt(data, action, POSITION_ACTION);
            ByteUtil.writeFloat(data, x, POSITION_X);
            ByteUtil.writeFloat(data, y, POSITION_Y);
            ByteUtil.writeInt(data, metaState, POSITION_META_STATE);

            inputHandler.obtainMessage(MODE_EVENT).sendToTarget();
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
                                Log.d(TAG, "exiting input module");
                                out.write(MODE_EXIT);
                                break;
                            case MODE_KEY:
                                out.write(MODE_KEY);
                                out.write(msg.arg1);
                                break;
                            case MODE_TEXT:
                                out.write(MODE_TEXT);
                                out.write(((String) msg.obj).getBytes(CHARSET_UTF_8));
                                break;
                            case MODE_EVENT:
                                socket.send(packet);
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
