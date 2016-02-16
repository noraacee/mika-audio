package com.mikaaudio.client.util;

import java.io.IOException;
import java.io.OutputStream;

public class Stream {
    public static final int LENGTH_BUFFER = 1024;

    public static int readInt(byte[] data, int offset) {
        return data[offset] << 24 | (data[offset + 1] & 0xFF) << 16 | (data[offset + 2] & 0xFF) << 8 | (data[offset + 3] & 0xFF);
    }

    public static void writeInt(OutputStream out, int data) throws IOException {
        out.write((byte) data >>> 24);
        out.write((byte) data >>> 16);
        out.write((byte) data >>> 8);
        out.write((byte) data);
    }
}
