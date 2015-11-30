package com.mikaaudio.client.util;

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

    private OutputStream key;
    private PrintWriter msgOut;

    public void setKeySocket(Socket socket) throws IOException {
        key = socket.getOutputStream();
    }

    public void setMessageSocket(Socket socket) throws IOException {
        msgOut = new PrintWriter(socket.getOutputStream(), true);
    }

    public void write(int key) {
        try {
            this.key.write(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String msg) {
        msgOut.println(msg);
    }
}
