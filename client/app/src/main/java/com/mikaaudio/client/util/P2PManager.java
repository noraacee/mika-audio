package com.mikaaudio.client.util;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;


import com.mikaaudio.client.activity.ClientActivity;

import java.io.IOException;
import java.net.Socket;

public class P2PManager {
    private static final String SERVICE_TYPE = "_http._tcp.";

    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;

    public P2PManager(final ClientActivity activity) {
        nsdManager = (NsdManager) activity.getSystemService(Context.NSD_SERVICE);

        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("start discover failed", Integer.toString(errorCode));
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("stop discovery failed", Integer.toString(errorCode));
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d("status", "discovery started");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d("status", "discover stopped");
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                if (!serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                    Log.e("service type", serviceInfo.getServiceType());
                } else if (serviceInfo.getServiceName().equals(SetupManager.getDeviceName())) {
                    nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            Log.e("resolve", Integer.toString(errorCode));
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo) {
                            try {
                                Socket socket = new Socket(serviceInfo.getHost(), serviceInfo.getPort());
                                activity.setSocket(socket);
                                nsdManager.stopServiceDiscovery(discoveryListener);
                            } catch (IOException e) {
                                Log.e("IOException", e.toString());
                            }
                        }
                    });
                } else {
                    Log.e("service name", serviceInfo.getServiceName());
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.d("status", "service lost");
            }
        };
    }

    public void connect() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }
}
