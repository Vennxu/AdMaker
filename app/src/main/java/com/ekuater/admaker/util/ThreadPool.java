package com.ekuater.admaker.util;

import android.support.annotation.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Leo on 2015/4/29.
 *
 * @author LinYong
 */
public final class ThreadPool {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    private static final ThreadPool sDefault = new ThreadPool();

    public static ThreadPool getDefault() {
        return sDefault;
    }

    private final ThreadFactory mThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "ThreadPool #" + mCount.getAndIncrement());
        }
    };
    private final BlockingQueue<Runnable> mPoolWorkQueue =
            new LinkedBlockingQueue<>(128);

    private final Executor mExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS,
            mPoolWorkQueue,
            mThreadFactory,
            new ThreadPoolExecutor.DiscardOldestPolicy());

    public ThreadPool() {
    }

    public void execute(Runnable command) {
        mExecutor.execute(command);
    }
}
