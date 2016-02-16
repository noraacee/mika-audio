package com.mikaaudio.client.interf;

import android.graphics.Bitmap;

public interface UICallbackListener {
    void onConnect();
    void onDisconnect();
    void onFrame(Bitmap frame);
    void onModuleChanged(int module);
}
