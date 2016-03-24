package com.mikaaudio.client.util;

import java.io.IOException;
import java.io.OutputStream;

public class Stream {
    public static int read(byte[] data, int offset) {
        return (data[offset] & 0xFF) << 16 | (data[offset + 1] & 0xFF) << 8 | (data[offset + 2] & 0xFF);
    }

    public static int readByte(byte[] data, int offset) {
        return (data[offset] & 0xFF);
    }

    public static int readInt(byte[] data) {
        return readInt(data, 0);
    }

    public static int readInt(byte[] data, int offset) {
        return data[offset] << 24 | (data[offset + 1] & 0xFF) << 16 | (data[offset + 2] & 0xFF) << 8 | (data[offset + 3] & 0xFF);
    }

    public static void writeInt(byte[] data, int value) {
        data[0] = (byte) (value >>> 24);
        data[1] = (byte) (value >>> 16);
        data[2] = (byte) (value >>> 8);
        data[3] = (byte) (value);
    }

    public static void writeInt(OutputStream out, int value) throws IOException {
        out.write(value >>> 24);
        out.write(value >>> 16);
        out.write(value >>> 8);
        out.write(value);
    }
}
