package com.mikaaudio.server.module;

import android.util.Log;

import com.mikaaudio.server.manager.ModuleManager;
import com.mikaaudio.server.util.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FrameModule {
    private static final String LIB_FRAME = "mikaframe";

    private boolean connected;

    private long instance;

    private InputStream in;

    public FrameModule() {
        connected = false;
        System.loadLibrary(LIB_FRAME);
    }

    public void stop() throws IOException {
        if (connected) {
            stop(instance);
            destroy(instance);
            connected = false;
            instance = -1;
            in = null;
        }
    }

    public void onDestroy() {
        destroy(instance);
    }

    public void start(InputStream in, OutputStream out) {
        try {
            if (!connected) {
                Log.d("status", "socket accepted");
                this.in = in;

                out.write(ModuleManager.ACK);

                if (init()) {
                    out.write(ModuleManager.ACK);
                    start();
                } else {
                    out.write(ModuleManager.REJECT);
                }
            } else {
                out.write(ModuleManager.REJECT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean init() throws IOException {
        String ip = Stream.readString(in);
        int port = Stream.readInt(in);
        int width = Stream.readInt(in);
        int height = Stream.readInt(in);

        instance = init(ip, port, width, height);

        return instance != -1;
    }

    private void pollStop() throws IOException {
        if (in.read() != ModuleManager.ACK)
            pollStop();
        else
            stop();
    }

    private void start() throws IOException {
        if (in.read() == ModuleManager.ACK) {
            new Thread(new FrameTask(instance)).start();
            pollStop();
        } else {
            start();
        }
    }

    private native long destroy(long screenPtr);
    private native long init(String ip, int port, int width, int height);
    private native void start(long screenPtr);
    private native void stop(long screenPtr);

    private class FrameTask implements Runnable {
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
