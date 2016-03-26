package com.mikaaudio.server.module;

import android.graphics.Bitmap;
import android.util.Log;

import com.mikaaudio.server.manager.ModuleManager;
import com.mikaaudio.server.util.Stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FrameModule {
    private static final int HEIGHT = 1280;
    private static final int WIDTH = 720;

    private static final int QUALITY_COMPRESSION = 50;

    private static final int SIZE_PACKET = 1500;
    private static final int SIZE_IP_HEADER = 20;
    private static final int SIZE_UDP_HEADER = 8;
    private static final int SIZE_FRAME_HEADER = 4;
    private static final int SIZE_DATA = SIZE_PACKET - SIZE_IP_HEADER - SIZE_UDP_HEADER - SIZE_FRAME_HEADER;

    private static final String LIB_FRAME = "mikaframe";
    private static final String TAG = "FRAME";

    private static volatile boolean connected;
    private static long instance;

    static {
        connected = false;
        instance = -1;
        System.loadLibrary(LIB_FRAME);
    }

    private FrameTask frameTask;

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

        frameTask = new FrameTask(instance, ip, port);

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
            new Thread(frameTask).start();
            Log.d(TAG, "started");
            pollStop(in);
        } else {
            start(in);
        }
    }

    private static native long destroy(long screenPtr);
    private static native long init(String ip, int port);
    private static native void stop(long screenPtr);
    private static native int updateFrame(long screenPtr, Bitmap frame);

    private static class FrameTask implements Runnable {
        private byte[] data;

        private int count;
        private int index;
        private int length;

        private long screenPtr;

        private Bitmap frame;
        private ByteArrayOutputStream bStream;
        private DatagramPacket packet;
        private DatagramSocket socket;

        public FrameTask(long screenPtr, String ipString, int port) throws IOException {
            this.screenPtr = screenPtr;
            socket = new DatagramSocket();

            InetAddress ip = InetAddress.getByName(ipString);
            data = new byte[SIZE_FRAME_HEADER + SIZE_DATA];
            packet = new DatagramPacket(data, SIZE_FRAME_HEADER + SIZE_DATA, ip, port);

            frame = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565);
            bStream = new ByteArrayOutputStream();
        }

        @Override
        public void run() {
            while (true) {
                if (updateFrame(screenPtr, frame) == 0) {
                    frame.compress(Bitmap.CompressFormat.JPEG, QUALITY_COMPRESSION, bStream);
                    byte[] bitmap = bStream.toByteArray();
                    bStream.reset();

                    count = 0;
                    while (count < bitmap.length) {
                        Stream.writeInt(data, count);

                        if (count + SIZE_DATA > bitmap.length)
                            length = bitmap.length - count;
                        else
                            length = SIZE_DATA;

                        for (index = 0; index < length; index++)
                            data[SIZE_FRAME_HEADER + index] = bitmap[count + index];

                        count += index;

                        if (count == bitmap.length)
                            data[0] = 0x01;
                        else
                            data[0] = 0x00;

                        packet.setData(data);
                        packet.setLength(index + SIZE_FRAME_HEADER);
                        try {
                            socket.send(packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
