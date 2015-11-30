package com.mikaaudio.client.util;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;


import com.mikaaudio.client.activity.ClientActivity;

import java.io.IOException;
import java.net.Socket;

public class P2PManager {
    private static final String KEY = "_KEY";
    private static final String MESSAGE = "_MSG";
    private static final String SERVICE_TYPE = "_http._tcp.";

    private boolean keyConnected;
    private boolean msgConnected;

    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;

    public P2PManager(final ClientActivity activity) {
        keyConnected = false;
        msgConnected = false;

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
                } else if (serviceInfo.getServiceName().contains(SetupManager.getDeviceName())) {
                    if (!keyConnected && serviceInfo.getServiceName().contains(KEY)) {
                        nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                            @Override
                            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                                Log.e("resolve", Integer.toString(errorCode));
                            }

                            @Override
                            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                                try {
                                    if (msgConnected)
                                        nsdManager.stopServiceDiscovery(discoveryListener);

                                    activity.setKeySocket(new Socket(serviceInfo.getHost(), serviceInfo.getPort()));
                                    Log.d("status", "connected key socket at host " + serviceInfo.getHost() + " and port " + serviceInfo.getPort());

                                    keyConnected = true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else if (!msgConnected && serviceInfo.getServiceName().contains(MESSAGE)) {
                        nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                            @Override
                            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                                Log.e("resolve", Integer.toString(errorCode));
                            }

                            @Override
                            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                                try {
                                    if (keyConnected)
                                        nsdManager.stopServiceDiscovery(discoveryListener);

                                    activity.setMsgSocket(new Socket(serviceInfo.getHost(), serviceInfo.getPort()));
                                    Log.d("status", "connected message socket at host " + serviceInfo.getHost() + " and port " + serviceInfo.getPort());

                                    msgConnected = true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
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
