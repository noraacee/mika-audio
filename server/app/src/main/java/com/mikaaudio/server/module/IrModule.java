package com.mikaaudio.server.module;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class IrModule {
    private static final int KEY_APPS = 187;
    private static final int KEY_BACK = 4;
    private static final int KEY_CLICK = 23;
    private static final int KEY_DOWN = 20;
    private static final int KEY_HOME = 3;
    private static final int KEY_LEFT = 21;
    private static final int KEY_RIGHT = 22;
    private static final int KEY_UP = 19;

    private static final int IR_KEY_CLICK = 0x00010065;
    private static final int IR_KEY_DOWN = 0x00010075;
    private static final int IR_KEY_UP = 0x00010074;
    private static final int IR_KEY_RIGHT = 0x00010033;
    private static final int IR_KEY_LEFT = 0x00010034;
    private static final int IR_KEY_HOME = 0x00010060;

    private static final Map<Integer, Integer> KEY_PROTOCOLS;

    static {
        KEY_PROTOCOLS = new HashMap<>();
        KEY_PROTOCOLS.put(IR_KEY_LEFT, KEY_LEFT);
        KEY_PROTOCOLS.put(IR_KEY_RIGHT, KEY_RIGHT);
        KEY_PROTOCOLS.put(IR_KEY_UP, KEY_UP);
        KEY_PROTOCOLS.put(IR_KEY_DOWN, KEY_DOWN);
        KEY_PROTOCOLS.put(IR_KEY_CLICK, KEY_CLICK);
        KEY_PROTOCOLS.put(IR_KEY_HOME, KEY_HOME);
    }

    private IrParser irParser;

    public IrModule() {
        try {
            BufferedInputStream iStream = new BufferedInputStream(new FileInputStream("/dev/input/event0"));
            irParser = new IrParser(iStream);
            new Thread(irParser).start();
        } catch (IOException e) {
            Log.d("IrCommunicationManager", "Failed to open IR device");
        }
    }

    public void onDestroy() {
        if (irParser != null)
            irParser.onDestroy();
    }

    private static class IrParser implements Runnable {
        private volatile boolean running;

        private BufferedInputStream iStream;

        public IrParser(BufferedInputStream iStream) {
            this.iStream = iStream;
            running = false;
        }

        public void onDestroy() {
            running = false;
        }

        @Override
        public void run() {
            final int MIN_DELAY = 500000; // in micro seconds
            int x, value;
            int prev_time_s = 0, prev_time_us = 0;
            short type, code;
            try {
                ByteBuffer buf = ByteBuffer.allocate(32);
                buf.order(ByteOrder.LITTLE_ENDIAN);

                while (running) {
                    // Read input
                    x = iStream.read(buf.array());
                    int time_s = buf.getInt(0);
                    int time_us = buf.getInt(4);
                    type = buf.getShort(8);
                    code = buf.getShort(10);
                    value = buf.getInt(12);

                    // check if more than .5s has elapsed then only input
                    if((time_us - prev_time_us)>=MIN_DELAY || (time_s > prev_time_s && ((time_us+1000000)-prev_time_us)>=MIN_DELAY)) {

                        // Save previous time
                        prev_time_s = time_s;
                        prev_time_us = time_us;

                        Log.d("IrCommunicationManager", "type: " + type
                                + " code: " + code
                                + " value: " + String.format("%x", value)
                                + " time_s: " + time_s
                                + " time_us: " + time_us);

                        // TODO: Do we care about the second half of the buffer?

                        // TODO: Limit number of inputs per second
                        // TODO: Confer with Aaron(aka twatface) about why it's so fucking slow
                        Integer key = KEY_PROTOCOLS.get(value);
                        if (key != null) {
                            InputModule.inputKeyEvent(key);
                        }
                    }
                }

            } catch (IOException e) {
                Log.d("IrCommunicationManager", "exception caught");
            }
        }
    }
}
