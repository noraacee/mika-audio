package com.mikaaudio.server.util;

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

    public void appendStatus(String status) {
        if (statusView != null) {
            String currStats = statusView.getText().toString();
            statusView.setText(currStats + "\n" + status);
        }
    }
}
