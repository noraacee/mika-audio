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

    private AcceptSocketTask acceptSocketTask;

    public P2PManager(Context context, CommunicationManager commManager) {
        this.commManager = commManager;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void onDestroy() {
        acceptSocketTask.cancel(true);
        nsdManager.unregisterService(new NsdManager.RegistrationListener() {
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
        });
    }

    public void registerService() throws IOException {
        ServerSocket keySocket = new ServerSocket(0);

        NsdServiceInfo cmdServiceInfo = new NsdServiceInfo();
        cmdServiceInfo.setServiceName(SetupManager.getDeviceName());
        cmdServiceInfo.setServiceType(SERVICE_TYPE);
        cmdServiceInfo.setPort(keySocket.getLocalPort());

        nsdManager.registerService(cmdServiceInfo, NsdManager.PROTOCOL_DNS_SD, new NsdManager.RegistrationListener() {
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
        });

        acceptSocketTask = new AcceptSocketTask(keySocket);
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
                    commManager.addKeySocket(serverSocket.accept());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
