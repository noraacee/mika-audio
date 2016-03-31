package com.mikaaudio.server.module;

import android.content.Context;
import android.graphics.Bitmap;
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
import java.net.InetAddress;

public class FrameModule {
    private static final int POSITION_ACTION = 0;
    private static final int POSITION_X = 4;
    private static final int POSITION_Y = 8;
    private static final int POSITION_META_STATE = 12;

    private static final int QUALITY_COMPRESSION = 50;

    private static final int SIZE_FRAME_PACKET = 1500;
    private static final int SIZE_INPUT_PACKET = 16;
    private static final int SIZE_IP_HEADER = 20;
    private static final int SIZE_UDP_HEADER = 8;
    private static final int SIZE_FRAME_HEADER = 4;
    private static final int SIZE_DATA = SIZE_FRAME_PACKET - SIZE_IP_HEADER - SIZE_UDP_HEADER - SIZE_FRAME_HEADER;

    private static final int THRESHOLD = 10000;
    private static final int TIMEOUT = 67;

    private static final String LIB_FRAME = "mikaframe";
    private static final String TAG = "FRAME";

    private static volatile boolean connected;
    private static long instance;

    static {
        connected = false;
        instance = -1;
        System.loadLibrary(LIB_FRAME);
    }

    private int screenHeight;
    private int screenWidth;

    private Context context;
    private DatagramPacket framePacket;
    private DatagramPacket inputPacket;
    private DatagramSocket frameSocket;
    private DatagramSocket inputSocket;
    private FrameTask frameTask;
    private InputModule inputModule;
    private InputTask inputTask;

    public FrameModule(Context context) {
        this.context = context;
    }

    public void stop() {
        if (connected) {
            Log.d(TAG, "stopping");
            destroy(instance);
            connected = false;
            instance = -1;
            Log.d(TAG, "stopped");
        }
    }

    public void onDestroy() {
        stop();
    }

    public void start(InputStream in, OutputStream out, InputModule inputModule, InetAddress localIp, InetAddress targetIp) {
        this.inputModule = inputModule;
        try {
            if (!connected) {
                connected = true;
                out.write(ModuleManager.ACK);

                if (init(in, out, localIp, targetIp)) {
                    Log.d(TAG, "initiailized");
                    out.write(ModuleManager.ACK);
                    start(in);
                } else {
                    Log.d(TAG, "failed to initialize");
                    out.write(ModuleManager.REJECT);
                    connected = false;
                }
            } else {
                out.write(ModuleManager.REJECT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean init(InputStream in, OutputStream out, InetAddress localIp, InetAddress targetIp) throws IOException {
        Log.d(TAG, "initializing");

        int framePort = ByteUtil.readInt(in);
        Log.d(TAG, "frame port: " + framePort);

        frameSocket = new DatagramSocket(0, localIp);
        framePacket = new DatagramPacket(new byte[SIZE_FRAME_HEADER + SIZE_DATA], SIZE_FRAME_HEADER + SIZE_DATA, targetIp, framePort);

        inputSocket = new DatagramSocket(0, localIp);
        inputPacket = new DatagramPacket(new byte[SIZE_INPUT_PACKET], SIZE_INPUT_PACKET);

        Log.d(TAG, "sending input port: " + inputSocket.getLocalPort());
        ByteUtil.writeInt(out, inputSocket.getLocalPort());

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        ByteUtil.writeInt(framePacket.getData(), screenWidth);
        ByteUtil.writeInt(framePacket.getData(), screenHeight, 4);

        frameSocket.send(framePacket);

        instance = init(screenWidth, screenHeight);

        frameTask = new FrameTask(instance, frameSocket, framePacket, screenWidth, screenHeight);

        return instance != -1;
    }

    private void parse() throws IOException {
        inputTask = new InputTask(inputSocket, inputPacket, inputModule, screenWidth, screenHeight);
        new Thread(inputTask).start();
    }

    private void start(InputStream in) throws IOException {
        if (in.read() == ModuleManager.ACK) {
            new Thread(frameTask).start();
            Log.d(TAG, "started");
            parse();
        } else {
            start(in);
        }
    }

    private static native long destroy(long screenPtr);
    private static native long init(int width, int height);
    private static native int updateFrame(long screenPtr, Bitmap frame);

    private static class InputTask implements Runnable {
        private volatile boolean running;

        private int screenHeight;
        private int screenWidth;

        private DatagramPacket packet;
        private DatagramSocket socket;
        private InputModule inputModule;

        public InputTask(DatagramSocket socket, DatagramPacket packet, InputModule inputModule, int screenWidth, int screenHeight) {
            this.socket = socket;
            this.packet = packet;
            this.inputModule = inputModule;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
        }

        @Override
        public void run() {
            running = true;
            try {
                byte[] data = packet.getData();
                long downTime = SystemClock.uptimeMillis();
                while (running) {
                    socket.receive(packet);
                    long eventTime = SystemClock.uptimeMillis();

                    int action = ByteUtil.readInt(data, POSITION_ACTION);
                    if (action == MotionEvent.ACTION_DOWN)
                        downTime = SystemClock.uptimeMillis();

                    float x = ByteUtil.readFloat(data, POSITION_X) * screenWidth;
                    float y = ByteUtil.readFloat(data, POSITION_Y) * screenHeight;

                    int metaState = ByteUtil.readInt(data, POSITION_META_STATE);

                    inputModule.inputMotionEvent(MotionEvent.obtain(downTime, eventTime, action, x, y, metaState));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class FrameTask implements Runnable {
        private byte[] data;

        private int count;
        private int index;

        private long screenPtr;

        private Bitmap frame;
        private ByteArrayOutputStream bStream;
        private DatagramPacket packet;
        private DatagramSocket socket;

        public FrameTask(long screenPtr, DatagramSocket socket, DatagramPacket packet, int screenWidth, int screenHeight) throws IOException {
            this.screenPtr = screenPtr;
            this.socket = socket;
            this.packet = packet;

            data = packet.getData();

            frame = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);
            bStream = new ByteArrayOutputStream();
        }

        @Override
        public void run() {
            int length;
            int diff = 0;
            while (true) {
                long instant = System.currentTimeMillis();
                if (updateFrame(screenPtr, frame) == 0) {
                    frame.compress(Bitmap.CompressFormat.JPEG, QUALITY_COMPRESSION, bStream);
                    byte[] bitmap = bStream.toByteArray();
                    bStream.reset();

                    if (diff != bitmap.length && bitmap.length >= THRESHOLD) {
                        diff = bitmap.length;

                        count = 0;
                        while (count < bitmap.length) {
                            ByteUtil.writeInt(data, count);

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

                            packet.setLength(index + SIZE_FRAME_HEADER);
                            try {
                                socket.send(packet);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        long timeout = instant + TIMEOUT - System.currentTimeMillis();
                        if (timeout > 0) {
                            try {
                                Thread.sleep(timeout);
                            } catch (InterruptedException ignored) {}
                        }
                    }
                }
            }
        }
    }
}
