package com.mikaaudio.server.util;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

public class P2PManager {
    private static final String SERVICE_TYPE = "_http._tcp.";

    private CommunicationManager commManager;
    private NsdManager nsdManager;
    private NsdManager.RegistrationListener registrationListener;

    private AcceptSocketTask acceptSocketTask;

    public P2PManager(Context context, CommunicationManager commManager) {
        this.commManager = commManager;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        registrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e("registration failed", Integer.toString(errorCode));
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e("unregistration failed", Integer.toString(errorCode));
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                Log.d("service name", serviceInfo.getServiceName());
                Log.d("status", "service registered");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                Log.d("status", "service unregistered");
            }
        };
    }

    public void onDestroy() {
        acceptSocketTask.cancel(true);
        nsdManager.unregisterService(registrationListener);
    }

    public void registerService() throws IOException {
        ServerSocket server = new ServerSocket(0);

        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(AppManager.getDeviceName());
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(server.getLocalPort());

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

        acceptSocketTask = new AcceptSocketTask(server);
        acceptSocketTask.execute();
    }

    private class AcceptSocketTask extends AsyncTask<Void, Void, Void> {
        private ServerSocket serverSocket;

        public AcceptSocketTask(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //noinspection InfiniteLoopStatement
                while(true)
                    commManager.addSocket(serverSocket.accept());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
