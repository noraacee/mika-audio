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
    private SuperUserManager suManager;

    private List<Socket> sockets;
    private List<ReadSocketTask> readSocketTasks;

    public CommunicationManager(SuperUserManager suManager) {
        this.suManager = suManager;

        sockets = new ArrayList<>();
        readSocketTasks = new ArrayList<>();
    }

    public void addKeySocket(Socket socket) throws IOException {
        Log.d("socket", "socket created at port " + socket.getLocalPort());

        socket.setSoTimeout(10000);
        sockets.add(socket);

        ReadSocketTask readSocketTask = new ReadSocketTask(socket);
        readSocketTask.execute();
        readSocketTasks.add(readSocketTask);
    }

    public void onDestroy() {
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
                int key;
                while(true) {
                    try {
                        key = in.read();
                        if (key != -1)
                            parse(key);
                        else
                            break;
                    } catch (SocketTimeoutException ste) {
                        Log.d("status", "Socket timeout");
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
            Log.d("status", "Socket disconnected");
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
