package com.mikaaudio.server.util;

import com.mikaaudio.server.manager.AppManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Stream {
    public static final int LENGTH_BUFFER = 1024;

    public static String readString(InputStream in) throws IOException {
        byte[] buffer = new byte[LENGTH_BUFFER];
        ByteArrayOutputStream inString = new ByteArrayOutputStream();

        int length;
        while((length = in.read(buffer)) != -1)
            inString.write(buffer, 0, length);

        return inString.toString(AppManager.getCharset());
    }

    public static String readString(InputStream in, ByteArrayOutputStream inString, byte[] buffer) throws IOException {
        int length;
        while((length = in.read(buffer)) != -1)
            inString.write(buffer, 0, length);
        return inString.toString(AppManager.getCharset());
    }

    public static int readInt(InputStream in) throws IOException {
        int b1, b2, b3, b4;

        b1 = in.read();
        b2 = in.read();
        b3 = in.read();
        b4 = in.read();

        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
    }
}
