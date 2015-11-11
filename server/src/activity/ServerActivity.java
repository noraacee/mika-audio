package com.mikaaudio.server.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.mikaaudio.server.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mikaaudio.server.util.StatusManager;
import com.mikaaudio.server.P2P.WiFiP2PBroadcastReceiver;

public class ServerActivity extends Activity{

    private TextView messageReceiveView;
    private EditText messageSendView;

    private List<WifiP2pDevice> peers;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDevice device;
    private WifiP2pConfig config;

    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        StatusManager.setStatusView((TextView) findViewById(R.id.status));

        messageReceiveView = (TextView) findViewById(R.id.message_received);
        messageSendView = (EditText) findViewById(R.id.message_to_send);

        peers = new ArrayList<>();

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiP2PBroadcastReceiver(manager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void setWiFiP2PEnabled(boolean enabled) {

    }

    public void updatePeerList(Collection<WifiP2pDevice> peers) {
        this.peers.clear();
        this.peers.addAll(peers);
    }

    private void connect() {
        device = peers.get(0);
        config = new WifiP2pConfig();

        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }
}
