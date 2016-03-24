package com.mikaaudio.server.module;

import android.util.Log;

import com.mikaaudio.server.manager.ModuleManager;
import com.mikaaudio.server.util.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FrameModule {
    private static final String LIB_FRAME = "mikaframe";
    private static final String TAG = "FRAME";

    private static volatile boolean connected;

    static {
        connected = false;
        System.loadLibrary(LIB_FRAME);
    }

    private static long instance;

    public void stop() {
        if (connected) {
            Log.d(TAG, "stopping");
            stop(instance);
            destroy(instance);
            connected = false;
            instance = -1;
            Log.d(TAG, "stopped");
        }
    }

    public void onDestroy() {
        stop();
    }

    public void start(InputStream in, OutputStream out, String ip) {
        try {
            if (!connected) {
                out.write(ModuleManager.ACK);

                if (init(in, ip)) {
                    Log.d(TAG, "initiailized");
                    out.write(ModuleManager.ACK);
                    start(in);
                } else {
                    Log.d(TAG, "failed to initialize");
                    out.write(ModuleManager.REJECT);
                }
            } else {
                out.write(ModuleManager.REJECT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean init(InputStream in, String ip) throws IOException {
        Log.d(TAG, "initializing");

        Log.d(TAG, "ip: " + ip);

        int port = Stream.readInt(in);
        Log.d(TAG, "port: " + port);

        instance = init(ip, port);

        return instance != -1;
    }

    private void pollStop(InputStream in) throws IOException {
        if (in.read() != ModuleManager.ACK)
            pollStop(in);
        else
            stop();
    }

    private void start(InputStream in) throws IOException {
        if (in.read() == ModuleManager.ACK) {
            new Thread(new FrameTask(instance)).start();
            Log.d(TAG, "started");
            pollStop(in);
        } else {
            start(in);
        }
    }

    private static native long destroy(long screenPtr);
    private static native long init(String ip, int port);
    private static native void start(long screenPtr);
    private static native void stop(long screenPtr);

    private static class FrameTask implements Runnable {
        private long screenPtr;

        public FrameTask(long screenPtr) {
            this.screenPtr = screenPtr;
        }

        @Override
        public void run() {
            start(screenPtr);
        }
    }
}
