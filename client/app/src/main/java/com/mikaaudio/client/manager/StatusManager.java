package com.mikaaudio.client.manager;

import android.widget.TextView;

public class StatusManager {
    private static StatusManager instance;

    static {
        instance = new StatusManager();
    }

    private TextView statusView;

    private StatusManager() {
    }

    public static StatusManager getInstance() {
        return instance;
    }

    public void setStatusView(TextView statusView) {
        this.statusView = statusView;
    }

    public void setStatus(String status) {
        if (statusView != null)
            statusView.setText(status);
    }
}
