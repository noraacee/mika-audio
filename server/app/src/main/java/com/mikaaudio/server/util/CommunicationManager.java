package com.mikaaudio.server.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class CommunicationManager {
    private static final int MODE_KEY = 0;
    private static final int MODE_STRING = 1;

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
        readSocketTasks.add(readSocketTask);
        readSocketTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    private void parse(String input) {
        Log.d("parsing", input);

        try {
            suManager.inputText(input);
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
                InputStream inByte = socket.getInputStream();
                BufferedReader inString = new BufferedReader(new InputStreamReader(inByte));
                OutputStream out = socket.getOutputStream();
                int cmd;
                String input;
                while(true) {
                    try {
                        cmd = inByte.read();
                        if (cmd == -1) {
                            break;
                        } else if (cmd == MODE_KEY) {
                            cmd = inByte.read();
                            if (cmd != -1)
                                parse(cmd);
                            else
                                break;
                        } else if (cmd == MODE_STRING) {
                            input = inString.readLine();
                            if (input != null)
                                parse(input);
                            else
                                break;
                        }
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
            Log.d("status", "socket at port " + socket.getLocalPort() + " is disconnected");

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
