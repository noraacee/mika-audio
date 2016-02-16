package com.mikaaudio.client.manager;

public class AppManager {
    private static final String CHARSET = "UTF_8";
    private static final String DEVICE_NAME = "MIKA AUDIO";

    public static String getCharset() { return CHARSET; }

    public static String getDeviceName() {
        return DEVICE_NAME;
    }
}
