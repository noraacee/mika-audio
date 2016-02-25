package com.mikaaudio.client.module;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.mikaaudio.client.manager.ModuleManager;
import com.mikaaudio.client.util.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FrameModule {
    private static final int SIZE_HEADER = 4;
    private static final int SIZE_PACKET = 65507;
    private static final int SIZE_PIXEL = 4;

    private static final int HEIGHT = 720;
    private static final int WIDTH = 480;
    private static final int PIXELS = WIDTH * HEIGHT;
    private static final int PIXELS_BYTES = PIXELS * SIZE_PIXEL;

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

    public boolean init(String ip) {
        try {
            Log.d("status", "initializing frame");

            Log.d("status", "socket created at ip: " + ip);
            connection = new DatagramSocket(0, InetAddress.getByName(ip));

            Log.d("status", "sending port: " + connection.getLocalPort());
            Stream.writeInt(out, connection.getLocalPort());

            Log.d("status", "sending width: " + WIDTH);
            Stream.writeInt(out, WIDTH);

            Log.d("status", "sending height: " + HEIGHT);
            Stream.writeInt(out, HEIGHT);

            Log.d("status", "done sending");

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

        Log.d("status", "starting frame");

        frameTask = new FrameTask(connection, frameHandler);
        new Thread(frameTask).start();

        try {
            out.write(ModuleManager.ACK);
            running = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class FrameTask implements Runnable {
        private volatile boolean running;

        private byte[] dataBuffer;
        private int[] colorBuffer;

        private DatagramSocket connection;
        private Handler frameHandler;

        public FrameTask(DatagramSocket connection, Handler frameHandler) {
            this.connection = connection;
            this.frameHandler = frameHandler;

            running = false;
            dataBuffer = new byte[SIZE_PACKET];
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
                    frame = receiveFrame();
                    frameHandler.obtainMessage(0, frame).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private Bitmap receiveFrame() throws IOException {
            DatagramPacket packet;

            boolean done = false;
            while (!done) {
                packet = new DatagramPacket(dataBuffer, SIZE_PACKET);
                connection.receive(packet);

                byte[] data = packet.getData();
                int index = Stream.readInt(data);

                for (int i = 1; i < packet.getLength() / 4; i++)
                    colorBuffer[index / SIZE_PIXEL + i - 1] = Stream.readInt(data, i * SIZE_PIXEL);

                if (index + packet.getLength() - SIZE_HEADER >= PIXELS_BYTES)
                    done = true;
            }

            return Bitmap.createBitmap(colorBuffer, WIDTH, HEIGHT, Bitmap.Config.RGB_565);
        }
    }
}
