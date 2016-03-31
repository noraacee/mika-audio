package com.mikaaudio.client.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by mcw91 on 3/31/2016.
 */
public class ButtonView extends ImageView {
    private static final float TRANSPARENCY = 0.5f;
    private boolean clickable;

    public ButtonView(Context context) {
        super(context);
        init();
    }

    public ButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (clickable) {
            if (ev.getAction() == KeyEvent.ACTION_DOWN)
                setAlpha(TRANSPARENCY);
            else if (ev.getAction() == KeyEvent.ACTION_UP)
                setAlpha(1f);
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public void setClickable(boolean clickable){
        super.setClickable(clickable);
        this.clickable = clickable;

        if (clickable)
            setAlpha(1f);
        else
            setAlpha(TRANSPARENCY);
    }

    private void init() {
        clickable = false;
        setAlpha(TRANSPARENCY);
    }
}
