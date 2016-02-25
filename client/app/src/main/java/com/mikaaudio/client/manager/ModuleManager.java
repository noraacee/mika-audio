package com.mikaaudio.client.manager;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.mikaaudio.client.interf.UICallbackListener;
import com.mikaaudio.client.module.FrameModule;
import com.mikaaudio.client.module.InputModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class ModuleManager {
    public static final int ACK = 0xFF;

    public static final int MODULE_EXIT = 0x00;
    public static final int MODULE_INPUT = 0x01;
    public static final int MODULE_FRAME = 0x02;

    private static final int HANDLER_CONNECTED = 0;
    private static final int HANDLER_DISCONNECTED = 1;
    private static final int HANDLER_MODULE = 2;

    private static final int SOCKET_TIMEOUT = 5000;

    private static final String TAG = "module";

    private boolean connected;

    private int currModule;

    private volatile FrameModule frameModule;
    private volatile InputModule inputModule;

    private UICallbackListener uiCallbackListener;

    private Handler uiHandler;
    private Socket socket;
    private ModuleThread moduleThread;

    public ModuleManager(UICallbackListener uiCallbackListener) {
        if (uiCallbackListener == null)
            throw new NullPointerException();
        this.uiCallbackListener = uiCallbackListener;

        uiHandler = new Handler(Looper.getMainLooper(), initUICallback());

        connected = false;
        currModule = MODULE_EXIT;
    }

    public FrameModule getFrameModule() {
        if (currModule != MODULE_FRAME)
            throw new IllegalStateException();
        return frameModule;
    }

    public InputModule getInputModule() {
        if (currModule != MODULE_INPUT)
            throw new IllegalStateException();
        return inputModule;
    }


    public void onDestroy() {
        if (connected) {
            frameModule.onDestroy();
            inputModule.onDestroy();

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            connected = false;
        }
    }

    public void switchModule(int module) {
        if (moduleThread == null)
            throw new IllegalStateException();

        moduleThread.switchModule(module);
    }

    public void setSocket(InetAddress ip, int port) throws IOException {
        socket = new Socket(ip, port);
        socket.setSoTimeout(SOCKET_TIMEOUT);

        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        moduleThread = new ModuleThread(in, out);

        inputModule = new InputModule(out);
        frameModule = new FrameModule(in, out, new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                uiCallbackListener.onFrame((Bitmap) msg.obj);
                return true;
            }
        }));

        uiHandler.obtainMessage(HANDLER_CONNECTED).sendToTarget();

        connected = true;
    }

    private Handler.Callback initUICallback() {
        return new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch(msg.what) {
                    case HANDLER_CONNECTED:
                        Log.d("status", "connected");
                        uiCallbackListener.onConnect();
                        break;
                    case HANDLER_DISCONNECTED:
                        Log.d("status", "disconnected");
                        uiCallbackListener.onDisconnect();
                        break;
                    case HANDLER_MODULE:
                        switch(msg.arg1) {
                            case MODULE_EXIT:
                                Log.d("status", "module exit");
                                connected = false;
                                currModule = MODULE_EXIT;
                                uiCallbackListener.onDisconnect();
                                break;
                            case MODULE_INPUT:
                                Log.d("status", "module input");
                                currModule = MODULE_INPUT;
                                uiCallbackListener.onModuleChanged(MODULE_INPUT);
                                break;
                            case MODULE_FRAME:
                                Log.d("status", "module frame");
                                currModule = MODULE_FRAME;
                                uiCallbackListener.onModuleChanged(MODULE_FRAME);
                                break;
                        }
                }
                return true;
            }
        };
    }

    private class ModuleThread extends HandlerThread {
        private int currModule;

        private InputStream in;
        private OutputStream out;

        private Handler moduleHandler;

        public ModuleThread(InputStream in, OutputStream out) {
            super(TAG);
            this.in = in;
            this.out = out;
            currModule = MODULE_EXIT;

            start();

            moduleHandler = new Handler(getLooper(), initCallback());
        }

        public void switchModule(int module) {
            moduleHandler.obtainMessage(module).sendToTarget();
        }

        private Handler.Callback initCallback() {
            return new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    try {
                        switch (msg.what) {
                            case MODULE_EXIT:
                                break;
                            case MODULE_INPUT:
                                Log.d("status", "switching to input");
                                switch (currModule) {
                                    case MODULE_FRAME:
                                        frameModule.stop();
                                        if (in.read() != ACK)
                                            break;
                                    case MODULE_EXIT:
                                        out.write(MODULE_INPUT);
                                        if (in.read() != ACK)
                                            break;
                                        currModule = MODULE_INPUT;
                                        uiHandler.obtainMessage(HANDLER_MODULE, MODULE_INPUT, 0).sendToTarget();
                                }
                                break;
                            case MODULE_FRAME:
                                Log.d("status", "switching to frame");
                                switch (currModule) {
                                    case MODULE_INPUT:
                                        inputModule.exit();
                                        if (in.read() != ACK)
                                            break;
                                    case MODULE_EXIT:
                                        Log.d("status", "connecting to frame");
                                        out.write(MODULE_FRAME);
                                        if (in.read() != ACK)
                                            break;
                                        if (frameModule.init(socket.getLocalAddress().getHostAddress()))
                                            uiHandler.obtainMessage(HANDLER_MODULE, MODULE_FRAME, 0).sendToTarget();
                                }
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return true;
                }
            };
        }
    }
}
