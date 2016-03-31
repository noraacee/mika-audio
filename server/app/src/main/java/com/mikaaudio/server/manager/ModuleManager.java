package com.mikaaudio.server.manager;

import android.content.Context;
import android.util.Log;

import com.mikaaudio.server.module.FrameModule;
import com.mikaaudio.server.module.InputModule;
import com.mikaaudio.server.module.IrModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final int ACK = 0xFF;
    public static final int REJECT = 0xFE;

    public static final int SOCKET_TIMEOUT = 5000;

    private static final int MODULE_EXIT = 0x00;
    private static final int MODULE_INPUT = 0x01;
    private static final int MODULE_FRAME = 0x02;

    private static final String TAG = "MODULE";

    private FrameModule frameModule;
    private IrModule irModule;
    private List<Connection> connections;

    public ModuleManager(Context context) {
        frameModule = new FrameModule(context);
        irModule = new IrModule(new InputModule());
        connections = new ArrayList<>();
    }

    public void addSocket(Socket socket) throws IOException {
        Log.d(TAG, "socket created at port " + socket.getLocalPort());

        Connection connection = new Connection();

        socket.setSoTimeout(SOCKET_TIMEOUT);
        CommunicationTask communication = new CommunicationTask(connection);

        connection.init(socket, communication);

        connections.add(connection);

        new Thread(communication).start();
    }

    public void onDestroy() {
        frameModule.onDestroy();
        irModule.onDestroy();

        for (Connection c : connections)
            c.onDestroy();
    }

    private class CommunicationTask implements Runnable {
        private volatile boolean running;
        private Connection connection;
        private InputModule inputModule;

        public CommunicationTask(Connection connection) {
            this.connection = connection;
            running = false;
        }

        public void onDestroy() {
            running = false;
            try {
                connection.getConnection().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = connection.getConnection().getInputStream();
                out = connection.getConnection().getOutputStream();

                inputModule = new InputModule();

                running = true;
                int module;
                Log.d(TAG, "listening");
                communication: while(running) {
                    try {
                        module = in.read();
                        switch(module) {
                            case MODULE_EXIT:
                                Log.d(TAG, "module exit");
                                out.write(ACK);
                                break communication;
                            case MODULE_INPUT:
                                Log.d(TAG, "module input");
                                inputModule.listen(in, out);
                                Log.d(TAG, "input stopped");
                                break;
                            case MODULE_FRAME:
                                Log.d(TAG, "module frame");
                                frameModule.start(in, out, inputModule,
                                        connection.getConnection().getLocalAddress(),
                                        connection.getConnection().getInetAddress());
                                break;
                        }
                    } catch (SocketTimeoutException e) {
                        Log.d(TAG, "timeout");
                        out.write(0);
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "disconnected");
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (out != null) {
                    try {
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            Log.d(TAG, "socket at port " + connection.getConnection().getLocalPort() + " is disconnected");

            try {
                connection.getConnection().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            connections.remove(connection);
        }
    }

    private class Connection {
        private Socket connection;
        private CommunicationTask communication;

        public void init(Socket connection, CommunicationTask communication) {
            this.connection = connection;
            this.communication = communication;
        }

        public Socket getConnection() {
            return connection;
        }


        public void onDestroy() {
            communication.onDestroy();
        }
    }
}
