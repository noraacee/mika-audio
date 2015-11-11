package com.mikaaudio.server.util;

import android.widget.TextView;

public class StatusManager {
    private static TextView statusView;

    public static void setStatusView(TextView statusView) {
        StatusManager.statusView = statusView;
    }

    public static void setStatus(String status) {
        if (statusView != null)
            StatusManager.statusView.setText(status);
    }
}
