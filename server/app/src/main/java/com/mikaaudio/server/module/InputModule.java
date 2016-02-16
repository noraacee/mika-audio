package com.mikaaudio.server.module;

import android.app.Instrumentation;
import android.util.Log;

import com.mikaaudio.server.manager.ModuleManager;
import com.mikaaudio.server.util.Stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputModule {
    private static final int MODE_EXIT = 0x00;
    private static final int MODE_KEY = 0x01;
    private static final int MODE_TEXT = 0x02;

    private byte[] buffer;

    private ByteArrayOutputStream inString;
    private InputStream in;
    private Instrumentation inputDispatcher;
    private OutputStream out;

    public InputModule(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;

        buffer = new byte[Stream.LENGTH_BUFFER];
        inString = new ByteArrayOutputStream();

        inputDispatcher = new Instrumentation();
    }

    public void inputKeyEvent(int key) {
        Log.d("status", "received key: " + key);
        inputDispatcher.sendKeyDownUpSync(key);
    }

    public void inputString(String text) {
        Log.d("status", "received key: " + text);
        inputDispatcher.sendStringSync(text);
    }

    public void listen() {
        try {
            out.write(ModuleManager.ACK);
            Log.d("status", "receiving input");

            int mode;
            listening: while (true) {
                mode = in.read();
                switch(mode) {
                    case MODE_EXIT:
                        break listening;
                    case MODE_KEY:
                        inputKeyEvent(in.read());
                        break;
                    case MODE_TEXT:
                        inputString(Stream.readString(in, inString, buffer));
                        break;
                }
            }

            out.write(ModuleManager.ACK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
