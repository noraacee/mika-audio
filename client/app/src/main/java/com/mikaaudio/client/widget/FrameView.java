package com.mikaaudio.client.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.nio.ByteBuffer;

public class FrameView extends SurfaceView implements SurfaceHolder.Callback {
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

    public int getSize() {
        return frameThread.getSize();
    }

    public void setDimensions(int width, int height) {
        frameThread.setDimensions(width, height);
    }

    public ByteBuffer getFrameBuffer() {
        return frameThread.getFrameBuffer();
    }

    public void ready() {
        frameThread.ready();
    }

    public void start() {
        frameThread.setRunning(true);
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
    }

    private void init() {
        SurfaceHolder holder = getHolder();
        holder.setFormat(PixelFormat.RGB_565);
        holder.addCallback(this);

        frameThread = new FrameThread(holder);
    }

    private class FrameThread extends Thread {
        private boolean running;

        private int size;

        private ByteBuffer buffer;
        private Bitmap pixels;
        private final SurfaceHolder holder;

        public FrameThread(SurfaceHolder holder) {
            this.holder = holder;

            running = false;
            buffer = null;
            pixels = null;
        }

        @Override
        public void run() {
            while(running) {
                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas(null);
                    synchronized (holder) {
                        if (running) {
                            try {
                                holder.wait();
                            } catch (InterruptedException ignored) {}

                            draw(canvas);
                            holder.notify();
                        }
                    }
                } finally {
                    if (canvas != null)
                        holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        public ByteBuffer getFrameBuffer() {
            return buffer;
        }

        public int getSize() {
            return size;
        }

        public void setDimensions(int width, int height) {
            synchronized (holder) {
                size = width * height;

                pixels = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                pixels.setDensity(1);
                buffer = ByteBuffer.allocate(pixels.getRowBytes() * height);
            }
        }

        public void ready() {
            synchronized (holder) {
                holder.notify();
                try {
                    holder.wait();
                } catch (InterruptedException ignored) {}
            }
        }

        public void setRunning(boolean running) {
            synchronized (holder) {
                this.running = running;
            }
        }

        private void draw(Canvas canvas) {
            if (pixels != null) {
                pixels.copyPixelsFromBuffer(buffer);
                canvas.drawBitmap(pixels, 0, 0, null);
            }
        }
    }
}
