package com.mikaaudio.client.interf;

public interface UICallbackListener {
    void onConnect();
    void onDisconnect();
    void onModuleChanged(int module);
}
