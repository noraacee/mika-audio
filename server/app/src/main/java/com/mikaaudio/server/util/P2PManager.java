package com.mikaaudio.server.util;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

public class P2PManager {
    private static final String COMMAND = "_CMD";
    private static final String MESSAGE = "_MSG";
    private static final String SERVICE_TYPE = "_http._tcp.";

    private NsdManager nsdManager;

    private ServerSocket cmdSocket;
    private ServerSocket msgSocket;

    public P2PManager(Context context) throws IOException {
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public ServerSocket getCmdSocket() {
        return cmdSocket;
    }

    public ServerSocket getMsgSocket() {
        return msgSocket;
    }

    public void registerCmdService() throws IOException {
        cmdSocket = new ServerSocket(0);

        NsdServiceInfo cmdServiceInfo = new NsdServiceInfo();
        cmdServiceInfo.setServiceName(SetupManager.getDeviceName() + COMMAND);
        cmdServiceInfo.setServiceType(SERVICE_TYPE);
        cmdServiceInfo.setPort(cmdSocket.getLocalPort());

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
    }

    public void registerMsgService() throws IOException {
        msgSocket = new ServerSocket(0);

        NsdServiceInfo msgServiceInfo = new NsdServiceInfo();
        msgServiceInfo.setServiceName(SetupManager.getDeviceName() + MESSAGE);
        msgServiceInfo.setServiceType(SERVICE_TYPE);
        msgServiceInfo.setPort(msgSocket.getLocalPort());

        nsdManager.registerService(msgServiceInfo, NsdManager.PROTOCOL_DNS_SD, new NsdManager.RegistrationListener() {
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
}
