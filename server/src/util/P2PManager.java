package com.mikaaudio.server.util;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.io.IOException;
import java.net.ServerSocket;

public class P2PManager {
    private static final String SERVICE_TYPE = "_http._tcp.";

    private NsdManager nsdManager;
    private ServerSocket socket;

    public P2PManager(Context context) throws IOException {
        socket = new ServerSocket(0);

        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SetupManager.getDeviceName());
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(socket.getLocalPort());

        NsdManager.RegistrationListener registrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {

            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {

            }
        };

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }
}
