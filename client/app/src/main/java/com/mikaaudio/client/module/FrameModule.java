package com.mikaaudio.client.module;

import android.graphics.Bitmap;
import android.os.Handler;

import com.mikaaudio.client.manager.AppManager;
import com.mikaaudio.client.manager.ModuleManager;
import com.mikaaudio.client.util.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class FrameModule {
    private static final int SIZE_HEADER = 8;
    private static final int SIZE_PACKET = 65507;

    private static final int HEIGHT = 720;
    private static final int WIDTH = 480;
    private static final int PIXELS = WIDTH * HEIGHT;
    private static final int PIXELS_BYTES = PIXELS * 4;

    private boolean running;

    private DatagramSocket connection;
    private InputStream in;
    private OutputStream out;

    private Handler frameHandler;
    private FrameTask frameTask;

    public FrameModule(InputStream in, OutputStream out, Handler frameHandler) {
        this.in = in;
        this.out = out;
        this.frameHandler = frameHandler;

        running = false;
    }

    public boolean init() {
        try {
            connection = new DatagramSocket();

            out.write(connection.getInetAddress().getHostAddress().getBytes(AppManager.getCharset()));
            Stream.writeInt(out, connection.getLocalPort());
            Stream.writeInt(out, WIDTH);
            Stream.writeInt(out, HEIGHT);

            return in.read() == ModuleManager.ACK;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void onDestroy() {
        if (running)
            stop();
    }

    public void stop() {
        try {
            out.write(ModuleManager.ACK);
            frameTask.stop();
            running = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (running)
            return;

        frameTask = new FrameTask(connection, frameHandler);
        new Thread(frameTask).start();

        try {
            out.write(ModuleManager.ACK);
            running = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap receiveFrame(DatagramSocket connection, byte[] buffer, int[] colorBuffer) throws IOException {
        byte[] dataBuffer = new byte[SIZE_PACKET];
        DatagramPacket packet;

        boolean done = false;
        while (!done) {
            packet = new DatagramPacket(dataBuffer, SIZE_PACKET);
            connection.receive(packet);
            byte[] data = packet.getData();
            int index = Stream.readInt(data, 0);

            System.arraycopy(data, 0, buffer, index, packet.getLength() - SIZE_HEADER);

            if (index + packet.getLength() == PIXELS_BYTES)
                done = true;
        }

        for (int i = 0; i < PIXELS; i++)
            colorBuffer[i] = Stream.readInt(buffer, i * 4);

        return Bitmap.createBitmap(colorBuffer, WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
    }

    private class FrameTask implements Runnable {
        private volatile boolean running;

        private byte[] buffer;
        private int[] colorBuffer;

        private DatagramSocket connection;
        private Handler frameHandler;

        public FrameTask(DatagramSocket connection, Handler frameHandler) {
            this.connection = connection;
            this.frameHandler = frameHandler;

            running = false;
            buffer = new byte[PIXELS_BYTES];
            colorBuffer = new int[PIXELS];
        }

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            running = true;
            Bitmap frame;
            while (running) {
                try {
                    frame = receiveFrame(connection, buffer, colorBuffer);
                    frameHandler.obtainMessage(0, frame).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
