package com.mikaaudio.server.manager;

import android.util.Log;

import com.mikaaudio.server.module.FrameModule;
import com.mikaaudio.server.module.InputModule;
import com.mikaaudio.server.module.IrModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final int ACK = 0xFF;
    public static final int REJECT = 0xFE;

    private static final int MODULE_EXIT = 0x00;
    private static final int MODULE_INPUT = 0x01;
    private static final int MODULE_FRAME = 0x02;

    private static final int SOCKET_TIMEOUT = 60000;

    private IrModule irModule;
    private List<Connection> connections;

    public ModuleManager() {
        irModule = new IrModule(new InputModule());
        connections = new ArrayList<>();
    }

    public void addSocket(Socket socket) throws IOException {
        Log.d("socket", "socket created at port " + socket.getLocalPort());

        Connection connection = new Connection();

        socket.setSoTimeout(SOCKET_TIMEOUT);
        CommunicationTask communication = new CommunicationTask(connection);

        connection.init(socket, communication);

        connections.add(connection);

        new Thread(communication).start();
    }

    public void onDestroy() {
        FrameModule.onDestroy();
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
            try {
                InputStream in = connection.getConnection().getInputStream();
                OutputStream out = connection.getConnection().getOutputStream();

                inputModule = new InputModule();

                running = true;
                int module;
                Log.d("status", "listening");
                communication: while(running) {
                    module = in.read();
                    switch(module) {
                        case MODULE_EXIT:
                            Log.d("status", "module exit");
                            out.write(ACK);
                            break communication;
                        case MODULE_INPUT:
                            Log.d("status", "module input");
                            inputModule.listen(in, out);
                            Log.d("status", "input stopped");
                            break;
                        case MODULE_FRAME:
                            Log.d("status", "module frame");
                            FrameModule.start(in, out, connection.getConnection().getInetAddress().getHostAddress());
                            break;
                    }
                }

                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("status", "socket at port " + connection.getConnection().getLocalPort() + " is disconnected");

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
