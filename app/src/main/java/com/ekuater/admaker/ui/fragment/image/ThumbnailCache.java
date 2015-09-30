package com.ekuater.admaker.ui.fragment.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.LruCache;

import com.ekuater.admaker.util.L;
import com.ekuater.admaker.util.ThreadPool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author LinYong
 */
/*package*/ class ThumbnailCache {

    private static final String TAG = ThumbnailCache.class.getSimpleName();

    public interface LoadCallback {
        void onThumbnailLoaded(Bitmap thumbnail);
    }

    private interface CallbackNotifier {
        void notify(LoadCallback callback);
    }

    private static ThumbnailCache sInstance;

    private static synchronized void initInstance() {
        if (sInstance == null) {
            sInstance = new ThumbnailCache();
        }
    }

    public static ThumbnailCache getInstance() {
        if (sInstance == null) {
            initInstance();
        }
        return sInstance;
    }

    private final ThreadPool mThreadPool;
    private final LruCache<String, Bitmap> mCache;
    private final Handler mHandler;

    private ThumbnailCache() {
        mThreadPool = ThreadPool.getDefault();
        mCache = new LruCache<>(50);
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void put(String path, Bitmap thumbnail) {
        if (!TextUtils.isEmpty(path) && thumbnail != null) {
            mCache.put(path, thumbnail);
        }
    }

    public void loadThumbnail(String thumbnailPath, String sourcePath, LoadCallback callback) {
        if (TextUtils.isEmpty(thumbnailPath) && TextUtils.isEmpty(sourcePath)) {
            notifyThumbnailLoaded(callback, null);
            return;
        }

        String key = TextUtils.isEmpty(thumbnailPath) ? sourcePath : thumbnailPath;
        Bitmap thumbnail = mCache.get(key);

        if (thumbnail != null) {
            notifyThumbnailLoaded(callback, thumbnail);
            return;
        }

        mThreadPool.execute(new ThumbnailLoader(thumbnailPath, sourcePath, callback));
    }

    private void notifyCallback(final CallbackNotifier notifier, final LoadCallback callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifier.notify(callback);
            }
        });
    }

    private void notifyThumbnailLoaded(LoadCallback callback, Bitmap thumbnail) {
        notifyCallback(new LoadedNotifier(thumbnail), callback);
    }

    private class LoadedNotifier implements CallbackNotifier {

        private final Bitmap mThumbnail;

        public LoadedNotifier(Bitmap thumbnail) {
            mThumbnail = thumbnail;
        }

        @Override
        public void notify(LoadCallback callback) {
            if (callback != null) {
                callback.onThumbnailLoaded(mThumbnail);
            }
        }
    }

    private class ThumbnailLoader implements Runnable {

        private final String mThumbnailPath;
        private final String mSourcePath;
        private final LoadCallback mCallback;

        public ThumbnailLoader(String thumbnailPath, String sourcePath,
                               LoadCallback callback) {
            mThumbnailPath = thumbnailPath;
            mSourcePath = sourcePath;
            mCallback = callback;
        }

        @Override
        public void run() {
            boolean thumbnailEmpty = TextUtils.isEmpty(mThumbnailPath);
            Bitmap thumbnail = thumbnailEmpty ? null : decodeBitmapFile(mThumbnailPath);
            String key = thumbnailEmpty ? mSourcePath : mThumbnailPath;

            if (thumbnail == null) {
                try {
                    thumbnail = generateThumbnail(mSourcePath);
                } catch (IOException | OutOfMemoryError e) {
                    L.w(TAG, e);
                }
            }

            put(key, thumbnail);
            notifyThumbnailLoaded(mCallback, thumbnail);
        }

        private Bitmap decodeBitmapFile(String path) {
            for (int i = 0; i < 3; ++i) {
                try {
                    return BitmapFactory.decodeFile(path);
                } catch (OutOfMemoryError error) {
                    System.gc();
                }
            }
            return null;
        }

        private Bitmap generateThumbnail(String sourcePath) throws IOException {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                    new File(sourcePath)));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();

            int i = 0;
            Bitmap bitmap;

            while (true) {
                if ((options.outWidth >> i <= 256)
                        && (options.outHeight >> i <= 256)) {
                    in = new BufferedInputStream(
                            new FileInputStream(new File(sourcePath)));
                    options.inSampleSize = (int) Math.pow(2.0D, i);
                    options.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeStream(in, null, options);
                    in.close();
                    break;
                }
                i += 1;
            }

            return bitmap;
        }
    }
}
