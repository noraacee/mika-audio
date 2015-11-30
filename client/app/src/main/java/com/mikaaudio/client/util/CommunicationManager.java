package com.mikaaudio.client.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class CommunicationManager {
    public static final int KEY_CLICK = 0;
    public static final int KEY_BACK = 1;
    public static final int KEY_HOME = 2;
    public static final int KEY_APPS = 3;
    public static final int KEY_UP = 4;
    public static final int KEY_DOWN = 5;
    public static final int KEY_LEFT = 6;
    public static final int KEY_RIGHT = 7;

    private OutputStream key;
    private PrintWriter msgOut;

    public void setKeySocket(Socket socket) throws IOException {
        key = socket.getOutputStream();
    }

    public void setMessageSocket(Socket socket) throws IOException {
        msgOut = new PrintWriter(socket.getOutputStream(), true);
    }

    public void write(int cmd) {
        try {
            key.write(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String msg) {
        msgOut.println(msg);
    }
}
