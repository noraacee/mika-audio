package com.mikaaudio.client.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class FrameView extends SurfaceView implements SurfaceHolder.Callback {
    private static final int TIMEOUT = 50;

    private double scale;

    private int height;
    private int pixelSize;
    private int width;

    private FrameThread frameThread;

    public FrameView(Context context) {
        super(context);
        init();
    }

    public FrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public byte[] getBuffer() {
        return frameThread.getBuffer();
    }

    public void setDimensions(int width, int height, int pixelSize) {
        this.width = width;
        this.height = height;
        this.pixelSize = pixelSize;
    }

    public void ready(int length) {
        frameThread.ready(length);
    }

    public void start() {
        SurfaceHolder holder = getHolder();
        holder.setFormat(PixelFormat.RGB_565);
        holder.addCallback(this);

        frameThread = new FrameThread(holder, width * height * pixelSize, scale);
        frameThread.start();
    }

    public void stop() {
        frameThread.setRunning(false);
        while (true) {
            try {
                frameThread.join();
                break;
            } catch (InterruptedException ignored) {}
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = getMeasuredWidth();
        scale = ((double) w) /  ((double) width);

        int h = (w / 9) * 16;

        setMeasuredDimension(w, h);

        if (frameThread != null)
            frameThread.setScale(scale);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        frameThread.setRunning(false);
        while (true) {
            try {
                frameThread.join();
                break;
            } catch (InterruptedException ignored) {}
        }

        frameThread = null;
    }

    private void init() {
        width = 0;
        height = 0;
        pixelSize = 0;
        scale = 1;
    }

    private class FrameThread extends Thread {
        private boolean running;

        private byte[] buffer;

        private int length;

        private Bitmap pixels;
        private BitmapFactory.Options frameOptions;
        private final SurfaceHolder holder;

        public FrameThread(SurfaceHolder holder, int size, double scale) {
            this.holder = holder;

            buffer = new byte[size];

            frameOptions = new BitmapFactory.Options();
            frameOptions.inScaled = true;
            frameOptions.inDither = true;
            setScale(scale);

            running = true;
            pixels = null;
        }

        @Override
        public void run() {
            synchronized (holder) {
                try {
                    holder.wait();
                } catch (InterruptedException ignored) { }
            }

            Canvas canvas = holder.lockCanvas(null);
            while(running) {
                synchronized (holder) {
                    if (running) {
                        draw(canvas);
                        holder.unlockCanvasAndPost(canvas);
                        holder.notify();

                        canvas = holder.lockCanvas(null);
                        try {
                            holder.wait();
                        } catch (InterruptedException ignored) {}
                    }
                }
            }
        }

        public byte[] getBuffer() {
            return buffer;
        }

        public void ready(int length) {
            this.length = length;
            synchronized (holder) {
                holder.notify();
                try {
                    holder.wait(TIMEOUT);
                } catch (InterruptedException ignored) {}
            }
        }

        public void setScale(double scale) {
            frameOptions.inDensity = 10;
            frameOptions.inTargetDensity = (int) (10.0 * scale);
        }

        public void setRunning(boolean running) {
            synchronized (holder) {
                this.running = running;
            }
        }

        private void draw(Canvas canvas) {
            pixels = BitmapFactory.decodeByteArray(buffer, 0, length, frameOptions);

            if (pixels != null)
                canvas.drawBitmap(pixels, 0, 0, null);
        }
    }
}
