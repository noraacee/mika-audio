package com.mikaaudio.client.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.LinearLayout;

import com.mikaaudio.client.interf.OnDispatchKeyEventListener;

public class InterceptKeyEventLinearLayout extends LinearLayout {
    private OnDispatchKeyEventListener onDispatchKeyEventListener;

    public InterceptKeyEventLinearLayout(Context context) {
        super(context);
    }

    public InterceptKeyEventLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptKeyEventLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (onDispatchKeyEventListener != null) {
            if (onDispatchKeyEventListener.onDispatchKeyEvent(keyEvent))
                return true;
        }

        return super.dispatchKeyEvent(keyEvent);
    }

    public void setOnDispatchKeyEventListener(OnDispatchKeyEventListener onDispatchKeyEventListener) {
        this.onDispatchKeyEventListener = onDispatchKeyEventListener;
    }
}
