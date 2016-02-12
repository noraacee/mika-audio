package com.mikaaudio.client.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.mikaaudio.client.interf.OnDisconnectListener;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class CommunicationManager {
    public static final int MODE_KEY = 0;
    public static final int MODE_STRING = 1;

    public static final int KEY_APPS = 187;
    public static final int KEY_BACK = 4;
    public static final int KEY_CLICK = 23;
    public static final int KEY_DOWN = 20;
    public static final int KEY_HOME = 3;
    public static final int KEY_LEFT = 21;
    public static final int KEY_RIGHT = 22;
    public static final int KEY_UP = 19;

    private byte[] frame;

    private boolean displaying;
    private boolean displayDone;

    private int frameSize;

    private OnDisconnectListener onDisconnectListener;

    private Bitmap bm;
    private DataInputStream in;
    private ImageView frameView;
    private OutputStream outByte;
    private PrintWriter outString;
    private Socket socket;

    public CommunicationManager(OnDisconnectListener onDisconnectListener) {
        this.onDisconnectListener = onDisconnectListener;

        displaying = false;
        displayDone = true;
    }

    public void onDestroy() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFrameView(ImageView frameView) {
        this.frameView = frameView;
    }

    public void setSocket(Socket socket) throws IOException {
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        outByte = socket.getOutputStream();
        outString = new PrintWriter(outByte, true);
    }

    public void toggleDisplay() {
        this.displaying = !displaying;
        if (displaying && displayDone)
            new ReadFrameTask().execute();
    }

    public class ReadFrameTask extends AsyncTask<Void, Void, Void> {
        public ReadFrameTask() {
            displayDone = false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                outByte.write(0x01);
                int len = in.readInt();
                if (len > 0) {
                    if (len != frameSize) {
                        frameSize = len;
                        frame = new byte[frameSize];
                    }

                    in.readFully(frame);

                    bm = BitmapFactory.decodeByteArray(frame, 0, len);
                    outByte.write(0x01);
                }
            } catch (IOException e) {
                bm = null;
                e.printStackTrace();
                displaying = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            if (bm != null)
                frameView.setImageBitmap(bm);

            try {
                TimeUnit.NANOSECONDS.sleep(750000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (displaying)
                new ReadFrameTask().execute();
            else
                displayDone = true;
        }
    }

    public void write(int key) {
        Log.d("sending", Integer.toString(key));

        try {
            outByte.write(MODE_KEY);
            outByte.write(key);
        } catch (IOException e) {
            onDisconnect();
        }
    }

    public void write(String input) {
        Log.d("sending", input);

        try {
            outByte.write(MODE_STRING);
            outString.println(input);
        } catch (IOException e) {
            onDisconnect();
        }
    }

    private void onDisconnect() {
        if (onDisconnectListener != null)
            onDisconnectListener.onDisconnect();
    }
}
