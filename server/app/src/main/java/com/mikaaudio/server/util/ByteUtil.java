package com.mikaaudio.server.util;

import com.mikaaudio.server.manager.AppManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteUtil {
    public static final int LENGTH_BUFFER = 1024;

    public static int readByte(byte[] data, int offset) {
        return (data[offset] & 0xFF);
    }

    public static float readFloat(byte[] data, int offset) {
        int intBits = readInt(data, offset);
        return Float.intBitsToFloat(intBits);
    }

    public static int readInt(byte[] data) {
        return readInt(data, 0);
    }

    public static int readInt(byte[] data, int offset) {
        return data[offset] << 24 | (data[offset + 1] & 0xFF) << 16 | (data[offset + 2] & 0xFF) << 8 | (data[offset + 3] & 0xFF);
    }

    public static int readInt(byte[] data, int offset, int n) {
        switch (n) {
            case 1:
                return (data[offset + 3] & 0xFF);
            case 2:
                return (data[offset + 2] & 0xFF) << 8 | (data[offset + 3] & 0xFF);
            case 3:
                return (data[offset + 1] & 0xFF) << 16 | (data[offset + 2] & 0xFF) << 8 | (data[offset + 3] & 0xFF);
            default:
                return data[offset] << 24 | (data[offset + 1] & 0xFF) << 16 | (data[offset + 2] & 0xFF) << 8 | (data[offset + 3] & 0xFF);
        }
    }

    public static int readInt(InputStream in) throws IOException {
        int b1, b2, b3, b4;

        b1 = in.read();
        b2 = in.read();
        b3 = in.read();
        b4 = in.read();

        return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
    }

    public static long readLong(byte[] data, int offset) {
        long value = 0;
        value += ((long) data[offset] & 0xFFL) << 56;
        value += ((long) data[offset + 1] & 0xFFL) << 48;
        value += ((long) data[offset + 2] & 0xFFL) << 40;
        value += ((long) data[offset + 3] & 0xFFL) << 32;
        value += ((long) data[offset + 4] & 0xFFL) << 24;
        value += ((long) data[offset + 5] & 0xFFL) << 16;
        value += ((long) data[offset + 6] & 0xFFL) << 8;
        value += ((long) data[offset + 7] & 0xFFL);

        return value;
    }

    public static String readString(InputStream in, ByteArrayOutputStream inString, byte[] buffer) throws IOException {
        int length;
        while((length = in.read(buffer)) != -1)
            inString.write(buffer, 0, length);
        return inString.toString(AppManager.getCharset());
    }

    public static void writeFloat(byte[] data, float value, int offset) {
        writeInt(data, Float.floatToIntBits(value), offset);
    }

    public static void writeInt(byte[] data, int value) {
        data[0] = (byte) (value >>> 24);
        data[1] = (byte) (value >>> 16);
        data[2] = (byte) (value >>> 8);
        data[3] = (byte) (value);
    }

    public static void writeInt(byte[] data, int value, int offset) {
        data[offset] = (byte) (value >>> 24);
        data[offset + 1] = (byte) (value >>> 16);
        data[offset + 2] = (byte) (value >>> 8);
        data[offset + 3] = (byte) (value);
    }

    public static void writeInt(OutputStream out, int value) throws IOException {
        out.write(value >>> 24);
        out.write(value >>> 16);
        out.write(value >>> 8);
        out.write(value);
    }

    public static void writeLong(byte[] data, long value, int offset) {
        data[offset] = (byte) (value >> 56);
        data[offset + 1] = (byte) (value >> 48);
        data[offset + 2] = (byte) (value >> 40);
        data[offset + 3] = (byte) (value >> 32);
        data[offset + 4] = (byte) (value >> 24);
        data[offset + 5] = (byte) (value >> 16);
        data[offset + 6] = (byte) (value >> 8);
        data[offset + 7] = (byte) value;
    }
}
