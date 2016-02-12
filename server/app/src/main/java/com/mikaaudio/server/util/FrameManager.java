package com.mikaaudio.server.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class FrameManager {
    public class InitException extends Exception {}

    private static final int HEIGHT = 720;
    private static final int WIDTH = 480;

    private static final int SOCKET_TIMEOUT = 60000;

    private static final String LIB_SERVER = "server";

    private SendFrameTask task;
    private Socket socket;

    public FrameManager() {
        System.loadLibrary(LIB_SERVER);
    }

    public void addSocket(Socket socket) {
        try {
            if (this.socket == null) {
                Log.d("status", "socket accepted");
                this.socket = socket;

                socket.setSoTimeout(SOCKET_TIMEOUT);

                task = new SendFrameTask();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                socket.close();
            }

        } catch (IOException | InitException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        if (task != null)
            task.cancel(true);
    }

    private class SendFrameTask extends AsyncTask<Void, Void, Void> {
        private long screenPtr;

        private Bitmap frame;
        private ByteArrayOutputStream bStream;

        public SendFrameTask() throws InitException {
            frame = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
            bStream = new ByteArrayOutputStream();

            screenPtr = init(WIDTH, HEIGHT);
            if (screenPtr == -1)
                throw new InitException();

            Log.d("status", "task created");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                InputStream in = socket.getInputStream();
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                int cmd;
                while(true) {
                    try {
                        cmd = in.read();
                        if (cmd == 0) {
                            break;
                        } else if (cmd == 0x01) {
                            if (updateFrame(screenPtr, frame) == 0) {
                                frame.compress(Bitmap.CompressFormat.JPEG, 50, bStream);
                                byte[] bytes = bStream.toByteArray();
                                bStream.reset();

                                out.writeInt(bytes.length);
                                out.write(bytes);
                                out.flush();

                                if (in.read() != 0x01)
                                    break;

                            }
                        }
                    }catch(SocketTimeoutException ste){
                        Log.d("status", "timeout");
                        out.write(0);
                    }
                }

                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                Log.d("status", "error");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            Log.d("status", "disconnected");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            socket = null;
        }
    }

    private native long init(int width, int height);
    private native int updateFrame(long screenPtr, Bitmap frame);
}
