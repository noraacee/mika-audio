package com.mikaaudio.client.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageManager {
    public static final int CLICK = 0;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;
    public static final int UP = 1;

    private OutputStream cmdOut;
    private PrintWriter msgOut;

    public void setCommandSocket(Socket socket) throws IOException {
        cmdOut = socket.getOutputStream();
    }

    public void setMessageSocket(Socket socket) throws IOException {
        msgOut = new PrintWriter(socket.getOutputStream(), true);
    }

    public void write(int cmd) {
        try {
            cmdOut.write(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String msg) {
        msgOut.println(msg);
    }
}
