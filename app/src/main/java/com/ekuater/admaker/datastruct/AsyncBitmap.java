package com.ekuater.admaker.datastruct;

import android.graphics.Bitmap;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Leo on 2015/6/25.
 *
 * @author LinYong
 */
public class AsyncBitmap {

    private Bitmap bitmap;
    private AtomicInteger lock;
    private AtomicBoolean needRecycle;

    public AsyncBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.lock = new AtomicInteger(0);
        this.needRecycle = new AtomicBoolean(false);
    }

    public synchronized void lock() {
        if (bitmap != null) {
            lock.incrementAndGet();
        }
    }

    public synchronized void unlock() {
        if (bitmap != null
                && lock.decrementAndGet() <= 0
                && needRecycle.get()) {
            recycleInternal();
        }
    }

    public synchronized void recycle() {
        if (lock.get() <= 0) {
            recycleInternal();
        } else {
            needRecycle.set(true);
        }
    }

    private void recycleInternal() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public synchronized Bitmap getBitmap() {
        return this.bitmap;
    }
}
