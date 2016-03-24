package com.mikaaudio.client.module;

import android.util.Log;

import com.mikaaudio.client.manager.ModuleManager;
import com.mikaaudio.client.util.Stream;
import com.mikaaudio.client.widget.FrameView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class FrameModule {
    private static final int SIZE_PIXEL = 2;
    private static final int SIZE_PACKET = 1500;
    private static final int SIZE_IP_HEADER = 20;
    private static final int SIZE_UDP_HEADER = 8;
    private static final int SIZE_FRAME_HEADER = 4;
    private static final int SIZE_DATA = SIZE_PACKET - SIZE_IP_HEADER - SIZE_UDP_HEADER - SIZE_FRAME_HEADER;

    private static final String TAG = "FRAME";

    private boolean running;

    private DatagramPacket packet;
    private DatagramSocket connection;
    private InputStream in;
    private OutputStream out;

    private FrameTask frameTask;
    private FrameView frameView;

    public FrameModule(InputStream in, OutputStream out, FrameView frameView) {
        this.in = in;
        this.out = out;
        this.frameView = frameView;

        running = false;
    }

    public boolean init(String ip) {
        try {
            Log.d(TAG, "initializing frame");

            Log.d(TAG, "socket created at ip: " + ip);
            connection = new DatagramSocket(0, InetAddress.getByName(ip));

            packet = new DatagramPacket(new byte[SIZE_DATA + SIZE_FRAME_HEADER], SIZE_DATA + SIZE_FRAME_HEADER);

            Log.d(TAG, "sending port: " + connection.getLocalPort());
            Stream.writeInt(out, connection.getLocalPort());

            Log.d(TAG, "done sending");

            connection.receive(packet);
            int width = Stream.readInt(packet.getData());
            int height = Stream.readInt(packet.getData(), 4);
            frameView.setDimensions(width,height);

            Log.d(TAG, "dimensions: " + width + ", " + height);

            int bufferSize = connection.getReceiveBufferSize();
            Log.d(TAG, "receive buffer: " + bufferSize);

            Stream.writeInt(packet.getData(), bufferSize);
            connection.send(packet);

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
            frameView.stop();
            frameTask.stop();
            running = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (running)
            return;

        Log.d(TAG, "starting frame");

        frameView.start();
        frameTask = new FrameTask(connection, packet, frameView);
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

        private byte[] data;

        private int pixels;

        private ByteBuffer frameBuffer;
        private DatagramPacket packet;
        private DatagramSocket connection;
        private FrameView frameView;

        public FrameTask(DatagramSocket connection, DatagramPacket packet, FrameView frameView) {
            this.connection = connection;
            this.packet = packet;
            this.frameView = frameView;

            running = false;
            data = packet.getData();
            pixels = frameView.getSize() * SIZE_PIXEL;
            frameBuffer = frameView.getFrameBuffer();
        }

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            running = true;
            int done;
            while (running) {
                try {
                    connection.receive(packet);

                    int index = Stream.read(data, 1) * SIZE_PIXEL;

                    frameBuffer.position(index);
                    frameBuffer.put(data, SIZE_FRAME_HEADER, packet.getLength() - SIZE_FRAME_HEADER);

                    done = Stream.readByte(data, 0);
                    if (done == 1) {
                        frameBuffer.limit(pixels);
                        frameBuffer.rewind();
                        frameView.ready();
                        frameBuffer.clear();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
