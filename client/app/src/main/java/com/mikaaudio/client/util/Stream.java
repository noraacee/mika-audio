package com.mikaaudio.client.util;

import java.io.IOException;
import java.io.OutputStream;

public class Stream {
    public static int readInt(byte[] data) {
        return data[0] << 24 | (data[1] & 0xFF) << 16 | (data[2] & 0xFF) << 8 | (data[3] & 0xFF);
    }

    public static int readInt(byte[] data, int offset) {
        return (data[offset + 3] & 0xFF) << 24 | (data[offset] & 0xFF) << 16 | (data[offset + 1] & 0xFF) << 8 | (data[offset + 2] & 0xFF);
    }

    public static void writeInt(OutputStream out, int data) throws IOException {
        out.write(data >>> 24);
        out.write(data >>> 16);
        out.write(data >>> 8);
        out.write(data);
    }
}
