package com.mikaaudio.client.module;

import android.util.Log;

import com.mikaaudio.client.manager.ModuleManager;
import com.mikaaudio.client.util.ByteUtil;
import com.mikaaudio.client.widget.FrameView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FrameModule {
    private static final int SIZE_PIXEL = 2;
    private static final int SIZE_PACKET = 1500;
    private static final int SIZE_IP_HEADER = 20;
    private static final int SIZE_UDP_HEADER = 8;
    private static final int SIZE_FRAME_HEADER = 4;
    private static final int SIZE_DATA = SIZE_PACKET - SIZE_IP_HEADER - SIZE_UDP_HEADER - SIZE_FRAME_HEADER;

    private static final String TAG = "FRAME";

    private boolean running;

    private DatagramPacket framePacket;
    private DatagramSocket frameConnection;
    private InputStream in;
    private OutputStream out;

    private FrameTask frameTask;
    private FrameView frameView;
    private InputModule inputModule;

    public FrameModule(InputStream in, OutputStream out, FrameView frameView, InputModule inputModule) {
        this.in = in;
        this.out = out;
        this.frameView = frameView;
        this.inputModule = inputModule;

        running = false;
    }

    public boolean init(InetAddress localIp, InetAddress targetIp) {
        try {
            Log.d(TAG, "initializing frame");

            frameConnection = new DatagramSocket(0, localIp);
            framePacket = new DatagramPacket(new byte[SIZE_DATA + SIZE_FRAME_HEADER], SIZE_DATA + SIZE_FRAME_HEADER);

            Log.d(TAG, "sending frame port: " + frameConnection.getLocalPort());
            ByteUtil.writeInt(out, frameConnection.getLocalPort());

            int inputPort = ByteUtil.readInt(in);
            Log.d(TAG, "input port: " + inputPort);

            inputModule.initFrameInput(targetIp, inputPort);
            frameView.setInputModule(inputModule);

            Log.d(TAG, "done sending");

            frameConnection.receive(framePacket);
            int width = ByteUtil.readInt(framePacket.getData());
            int height = ByteUtil.readInt(framePacket.getData(), 4);
            frameView.setDimensions(width, height, SIZE_PIXEL);


            Log.d(TAG, "dimensions: " + width + ", " + height);

            int bufferSize = frameConnection.getReceiveBufferSize();
            Log.d(TAG, "receive buffer: " + bufferSize);

            ByteUtil.writeInt(framePacket.getData(), bufferSize);
            frameConnection.send(framePacket);

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

    public void start() {
        if (running)
            return;

        Log.d(TAG, "starting frame");

        frameView.start();
        frameTask = new FrameTask(frameConnection, framePacket, frameView);
        new Thread(frameTask).start();

        try {
            out.write(ModuleManager.ACK);
            running = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private static class FrameTask implements Runnable {
        private volatile boolean running;

        private byte[] buffer;
        private byte[] data;

        private DatagramPacket packet;
        private DatagramSocket connection;
        private FrameView frameView;

        public FrameTask(DatagramSocket connection, DatagramPacket packet, FrameView frameView) {
            this.connection = connection;
            this.packet = packet;
            this.frameView = frameView;

            running = false;
            data = packet.getData();

            buffer = frameView.getBuffer();
        }

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            running = true;
            int done;
            int length;
            while (running) {
                try {
                    packet.setLength(SIZE_DATA + SIZE_FRAME_HEADER);
                    connection.receive(packet);

                    int index = ByteUtil.readInt(data, 1, 3);
                    System.arraycopy(data, SIZE_FRAME_HEADER, buffer, index, packet.getLength() - SIZE_FRAME_HEADER);

                    done = ByteUtil.readByte(data, 0);
                    if (done == 1) {
                        length = index + packet.getLength() - SIZE_FRAME_HEADER;
                        frameView.ready(length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
