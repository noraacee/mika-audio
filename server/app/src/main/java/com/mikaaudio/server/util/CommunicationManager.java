package com.mikaaudio.server.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class CommunicationManager {
    private static final int SOCKET_TIMEOUT = 60000;
    private SuperUserManager suManager;

    private List<Socket> sockets;
    private List<ReadSocketTask> readSocketTasks;

    public CommunicationManager(SuperUserManager suManager) {
        this.suManager = suManager;

        sockets = new ArrayList<>();
        readSocketTasks = new ArrayList<>();
    }

    public void addSocket(Socket socket) throws IOException {
        Log.d("socket", "socket created at port " + socket.getLocalPort());

        socket.setSoTimeout(SOCKET_TIMEOUT);
        sockets.add(socket);

        ReadSocketTask readSocketTask = new ReadSocketTask(socket);
        readSocketTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        readSocketTasks.add(readSocketTask);
    }

    public void onDestroy() {
        Log.d("status", "On destroy");
        for (ReadSocketTask t : readSocketTasks)
            t.cancel(true);

        for (Socket s : sockets) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parse(int key) {
        Log.d("parsing", Integer.toString(key));
        try {
            suManager.inputKeyEvent(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReadSocketTask extends AsyncTask<Void, Void, Void> {
        private Socket socket;

        public ReadSocketTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        protected Void doInBackground(Void... nothing) {
            try {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                int cmd;
                while(true) {
                    try {
                        cmd = in.read();
                        if (cmd != -1)
                            parse(cmd);
                        else
                            break;
                    } catch (SocketTimeoutException ste) {
                        try {
                            out.write(0);
                        } catch (IOException ioe) {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            Log.d("status", "Socket at port " + socket.getLocalPort() + "is disconnected");
            sockets.remove(socket);
            readSocketTasks.remove(this);

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
