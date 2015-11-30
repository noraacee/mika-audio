package com.mikaaudio.client.util;

import com.mikaaudio.client.interf.OnDisconnectListener;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class CommunicationManager {
    public static final int KEY_APPS = 187;
    public static final int KEY_BACK = 4;
    public static final int KEY_CLICK = 23;
    public static final int KEY_DOWN = 20;
    public static final int KEY_HOME = 3;
    public static final int KEY_LEFT = 21;
    public static final int KEY_RIGHT = 22;
    public static final int KEY_UP = 19;

    private OnDisconnectListener onDisconnectListener;

    private OutputStream outByte;
    private PrintWriter outString;
    private Socket socket;

    public CommunicationManager(OnDisconnectListener dListener) {
        this.onDisconnectListener = dListener;
    }

    public void onDestroy() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSocket(Socket socket) throws IOException {
        this.socket = socket;
        outByte = socket.getOutputStream();
        outString = new PrintWriter(outByte);
    }

    public void write(int cmd) {
        try {
            outByte.write(cmd);
        } catch (IOException e) {
            onDisconnect();
        }
    }

    private void onDisconnect() {
        if (onDisconnectListener != null)
            onDisconnectListener.onDisconnect();
    }
}
