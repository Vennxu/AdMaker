package com.ekuater.admaker.delegate.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by Leo on 2015/1/8.
 *
 * @author Linyong
 */
public final class OnlineImageLoader {

    private static final int DISK_CACHE_SIZE = 200 * 1024 * 1024; // 200 MB

    private static OnlineImageLoader sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new OnlineImageLoader(context.getApplicationContext());
        }
    }

    public static OnlineImageLoader getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final Context mContext;
    private final ImageLoader mImageLoader;
    private final DisplayImageOptions mNoMemCacheDisplayOptions;
    private final DisplayImageOptions mMemCacheDisplayOptions;

    private OnlineImageLoader(Context context) {
        mContext = context;
        mImageLoader = ImageLoader.getInstance();
        mNoMemCacheDisplayOptions = newNoMemCacheDisplayOptions();
        mMemCacheDisplayOptions = newMemCacheDisplayOptions();
        initImageLoader();
    }

    private DisplayImageOptions newNoMemCacheDisplayOptions() {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(false);
        return builder.build();
    }

    private DisplayImageOptions newMemCacheDisplayOptions() {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(false);
        return builder.build();
    }

    private void initImageLoader() {
        ImageLoaderConfiguration.Builder builder
                = new ImageLoaderConfiguration.Builder(mContext)
                .memoryCache(new LruMemoryCache(50))
                .denyCacheImageMultipleSizesInMemory()
                .threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(DISK_CACHE_SIZE)
                .defaultDisplayImageOptions(mMemCacheDisplayOptions)
                .tasksProcessingOrder(QueueProcessingType.FIFO);
        mImageLoader.init(builder.build());
    }

    /**
     * Clear avatar cache in memory and disk
     */
    public void clearCache() {
        mImageLoader.clearMemoryCache();
        mImageLoader.clearDiskCache();
    }

    public synchronized Bitmap getImageBitmap(String fullUrl,
                                              OnlineImageLoadListener listener) {
        GetImageBitmapListener loadListener = new GetImageBitmapListener(listener);
        loadImage(fullUrl, null, mNoMemCacheDisplayOptions, loadListener, null);
        return loadListener.getLoadedBitmapOrStartListener();
    }

    public synchronized Bitmap getMemCacheImageBitmap(String fullUrl,
                                                      OnlineImageLoadListener listener) {
        GetImageBitmapListener loadListener = new GetImageBitmapListener(listener);
        loadImage(fullUrl, null, mMemCacheDisplayOptions, loadListener, null);
        return loadListener.getLoadedBitmapOrStartListener();
    }

    public Bitmap loadImage(String fullUrl, TargetSize targetSize,
                            DisplayOptions displayOptions,
                            OnlineImageLoadListener listener) {
        GetImageBitmapListener loadListener = new GetImageBitmapListener(listener);
        ImageSize imageSize = targetSize != null ? toImageSize(targetSize) : null;
        DisplayImageOptions options = displayOptions != null
                ? toDisplayImageOptions(displayOptions) : null;
        loadImage(fullUrl, imageSize, options, loadListener, null);
        return loadListener.getLoadedBitmapOrStartListener();
    }

    private void loadImage(String uri, ImageSize targetImageSize,
                           DisplayImageOptions options,
                           ImageLoadingListener listener,
                           ImageLoadingProgressListener progressListener) {
        if (targetImageSize == null) {
            targetImageSize = getMaxImageSize();
        }

        LoadNonViewAware imageAware = new LoadNonViewAware(uri,
                targetImageSize, ViewScaleType.CROP);
        mImageLoader.displayImage(uri, imageAware, options,
                listener, progressListener);
    }

    private ImageSize getMaxImageSize() {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        return new ImageSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    public void displayImage(String fullUrl, ImageView imageView) {
        displayImage(fullUrl, imageView, null, null);
    }

    public void displayImage(String fullUrl, ImageView imageView,
                             DisplayOptions displayOptions) {
        displayImage(fullUrl, imageView, displayOptions, null);
    }

    public void displayImage(String fullUrl, ImageView imageView,
                             OnlineImageLoadListener listener) {
        displayImage(fullUrl, imageView, null, listener);
    }

    public void displayImage(String fullUrl, ImageView imageView,
                             DisplayOptions displayOptions,
                             OnlineImageLoadListener listener) {
        DisplayImageListener loadListener = (listener != null)
                ? new DisplayImageListener(listener) : null;
        DisplayImageOptions options = displayOptions != null
                ? toDisplayImageOptions(displayOptions) : null;
        mImageLoader.displayImage(fullUrl, imageView, options, loadListener);
    }

    private static LoadFailType toFailType(FailReason failReason) {
        LoadFailType loadFailType;

        switch (failReason.getType()) {
            case IO_ERROR:
                loadFailType = LoadFailType.IO_ERROR;
                break;
            case DECODING_ERROR:
                loadFailType = LoadFailType.DECODING_ERROR;
                break;
            case NETWORK_DENIED:
                loadFailType = LoadFailType.NETWORK_DENIED;
                break;
            case OUT_OF_MEMORY:
                loadFailType = LoadFailType.OUT_OF_MEMORY;
                break;
            default:
                loadFailType = LoadFailType.UNKNOWN;
                break;
        }

        return loadFailType;
    }

    private static class LoadNonViewAware extends NonViewAware {

        public LoadNonViewAware(String imageUri, ImageSize imageSize, ViewScaleType scaleType) {
            super(imageUri, imageSize, scaleType);
        }

        @Override
        public int getId() {
            return hashCode();
        }
    }

    private static class GetImageBitmapListener extends SimpleImageLoadingListener {

        private final OnlineImageLoadListener mListener;
        private Bitmap mLoadedImage;
        private boolean mListening;

        public GetImageBitmapListener(OnlineImageLoadListener listener) {
            mListener = listener;
            mLoadedImage = null;
            mListening = false;
        }

        private void notifyLoadFailed(String imageUri, LoadFailType loadFailType) {
            mListener.onLoadFailed(imageUri, loadFailType);
        }

        private void notifyLoadComplete(String imageUri, Bitmap loadedImage) {
            mListener.onLoadComplete(imageUri, loadedImage);
        }

        @Override
        public synchronized void onLoadingFailed(String imageUri, View view,
                                                 FailReason failReason) {
            if (mListening) {
                notifyLoadFailed(imageUri, toFailType(failReason));
            }
        }

        @Override
        public synchronized void onLoadingComplete(String imageUri, View view,
                                                   Bitmap loadedImage) {
            mLoadedImage = loadedImage;
            if (mListening) {
                notifyLoadComplete(imageUri, mLoadedImage);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            if (mListening) {
                notifyLoadFailed(imageUri, LoadFailType.CANCELLED);
            }
        }

        public synchronized Bitmap getLoadedBitmapOrStartListener() {
            mLoadedImage = (mLoadedImage == null || mLoadedImage.isRecycled())
                    ? null : mLoadedImage;
            mListening = (mLoadedImage == null);
            return mLoadedImage;
        }
    }

    private static class DisplayImageListener implements ImageLoadingListener {

        private final OnlineImageLoadListener mListener;

        public DisplayImageListener(OnlineImageLoadListener listener) {
            mListener = listener;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            if (mListener != null) {
                mListener.onLoadFailed(imageUri, toFailType(failReason));
            }
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (mListener != null) {
                mListener.onLoadComplete(imageUri, loadedImage);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            if (mListener != null) {
                mListener.onLoadFailed(imageUri, LoadFailType.CANCELLED);
            }
        }
    }

    private static ImageSize toImageSize(TargetSize targetSize) {
        return new ImageSize(targetSize.getWidth(), targetSize.getHeight());
    }

    private static DisplayImageOptions toDisplayImageOptions(DisplayOptions displayOptions) {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder()
                .showImageOnLoading(displayOptions.getDefaultImageRes())
                .showImageForEmptyUri(displayOptions.getDefaultImageRes())
                .showImageOnFail(displayOptions.getDefaultImageRes())
                .cacheInMemory(displayOptions.isCacheInMemory())
                .cacheOnDisk(displayOptions.isCacheOnDisk())
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565);
        return builder.build();
    }
}
