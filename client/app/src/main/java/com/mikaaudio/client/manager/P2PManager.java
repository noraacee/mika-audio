package com.mikaaudio.client.manager;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.IOException;

public class P2PManager {
    private static final String SERVICE_TYPE = "_http._tcp.";

    private boolean discovering;

    private ModuleManager moduleManager;
    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;

    public P2PManager(Context context, ModuleManager moduleManager) {
        this.moduleManager = moduleManager;

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        discovering = false;
        discoveryListener = initDiscoveryListener();
    }

    public void connect() {
        if (!discovering) {
            discovering = true;
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        }
    }

    public void onDestroy() {
        if (discovering) {
            discovering = false;
            nsdManager.stopServiceDiscovery(discoveryListener);
        }
    }

    private NsdManager.DiscoveryListener initDiscoveryListener() {
        return new NsdManager.DiscoveryListener() {
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
                Log.d("status", "discovery stopped");
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                if (!serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                    Log.e("service type", serviceInfo.getServiceType());
                } else if (serviceInfo.getServiceName().contains(AppManager.getDeviceName())) {
                    Log.d("status", "connecting to " + serviceInfo.getServiceName());
                    nsdManager.resolveService(serviceInfo, initResolveListener());
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

    private NsdManager.ResolveListener initResolveListener() {
        return new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e("resolve", Integer.toString(errorCode));
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                try {
                    moduleManager.setSocket(serviceInfo.getHost(), serviceInfo.getPort());

                    if (discovering)
                        nsdManager.stopServiceDiscovery(discoveryListener);
                    discovering = false;


                    Log.d("status", "connected socket at host " + serviceInfo.getHost() + " and port " + serviceInfo.getPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
