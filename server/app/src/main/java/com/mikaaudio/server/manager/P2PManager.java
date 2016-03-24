package com.mikaaudio.server.manager;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;


import java.io.IOException;
import java.net.ServerSocket;

public class P2PManager {
    private static final String SERVICE_TYPE = "_http._tcp.";
    private static final String TAG = "P2P";

    private ModuleManager moduleManager;
    private NsdManager nsdManager;
    private NsdManager.RegistrationListener registrationListener;
    private AcceptClientTask acceptClientTask;

    public P2PManager(Context context) {
        moduleManager = new ModuleManager();


        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        registrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "registration failed with error code: " + errorCode);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "unregistration failed with error code: " + errorCode);
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "service name: " + serviceInfo.getServiceName());
                Log.d(TAG, "service registered");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "service unregistered");
            }
        };
    }

    public void onDestroy() {
        acceptClientTask.onDestroy();
        moduleManager.onDestroy();
        nsdManager.unregisterService(registrationListener);
    }

    public void registerService() throws IOException {
        acceptClientTask = new AcceptClientTask();
        ServerSocket server = acceptClientTask.getServerSocket();

        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(AppManager.getDeviceName());
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(server.getLocalPort());

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

        new Thread(acceptClientTask).start();
    }

    private class AcceptClientTask implements Runnable {
        private volatile boolean running;
        private volatile ServerSocket serverSocket;

        public AcceptClientTask() throws IOException {
            running = false;
            serverSocket = new ServerSocket(0);
        }

        public ServerSocket getServerSocket() {
            return serverSocket;
        }

        public void onDestroy() {
            running = false;

            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                running = true;
                while(running) {
                    Log.d(TAG, "accepting connections");
                    moduleManager.addSocket(serverSocket.accept());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
