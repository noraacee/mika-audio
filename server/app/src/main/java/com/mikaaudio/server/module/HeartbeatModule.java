package com.mikaaudio.server.module;

import com.mikaaudio.server.manager.ModuleManager;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HeartbeatModule {
    private static final int INTERVAL_HEARTBEAT = 5000;

    private static final String TAG = "HEARTBEAT";

    private HeartbeatOutThread heartbeatThread;

    public HeartbeatModule() {
        heartbeatThread = new HeartbeatOutThread();
        new Thread(heartbeatThread).start();
    }

    public void addConnection(Socket socket) {
        heartbeatThread.addConnection(socket);
    }

    public void onDestroy() {
        heartbeatThread.stop();
    }

    private static class HeartbeatOutThread implements Runnable {
        private volatile boolean running;

        private Map<Socket, Long> connections;
        private final ConcurrentLinkedQueue<Socket> queue;

        public HeartbeatOutThread() {
            connections = new HashMap<>();
            queue = new ConcurrentLinkedQueue<>();
        }


        public void addConnection(Socket socket) {
            connections.put(socket, System.currentTimeMillis() + INTERVAL_HEARTBEAT);
            queue.add(socket);

            synchronized (queue) {
                queue.notify();
            }
        }

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            running = true;
            while (running) {
                try {
                    if (queue.isEmpty()) {
                        synchronized (queue) {
                            queue.wait();
                        }
                    } else {
                        Socket socket = queue.poll();
                        Long wait = connections.get(socket);
                        if (wait == null)
                            continue;

                        long instant = wait - System.currentTimeMillis();
                        if (instant > 0)
                            wait(instant);


                        socket.getOutputStream().write(ModuleManager.ACK);

                        connections.put(socket, System.currentTimeMillis() + INTERVAL_HEARTBEAT);
                        queue.add(socket);
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
