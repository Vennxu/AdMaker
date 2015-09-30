package com.ekuater.admaker.delegate;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.ImageView;

import com.ekuater.admaker.EnvConfig;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.datastruct.Scene;
import com.ekuater.admaker.delegate.imageloader.CommunityImageLoadListener;
import com.ekuater.admaker.delegate.imageloader.DisplayOptions;
import com.ekuater.admaker.delegate.imageloader.LoadFailType;
import com.ekuater.admaker.delegate.imageloader.OnlineImageLoadListener;
import com.ekuater.admaker.delegate.imageloader.OnlineImageLoader;
import com.ekuater.admaker.delegate.imageloader.SelectHotImageLoadListener;

import java.util.Locale;

/**
 * Created by Leo on 2015/6/17.
 *
 * @author LinYong
 */
public class AdElementDisplay {

    private static final String INTERNAL_STICKER_DIR = "assets://ad_stickers/";
    private static final String LOCAL_STICKER_DIR = "file://"
            + EnvConfig.CUSTOM_STICKERS_DIR.getAbsolutePath() + "/";

    public interface BitmapLoadListener {
        void onLoaded(Object object, boolean success, Bitmap[] bitmaps);
    }

    private volatile static AdElementDisplay sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new AdElementDisplay(context.getApplicationContext());
        }
    }

    public static AdElementDisplay getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final String mImageBaseUrl;
    private final OnlineImageLoader mImageLoader;

    private AdElementDisplay(Context context) {
        mImageBaseUrl = context.getString(R.string.config_image_base_url);
        mImageLoader = OnlineImageLoader.getInstance(context);
    }

    /**
     * Display sticker thumb in imageView
     *
     * @param sticker   sticker
     * @param imageView imageView
     */
    public void displayStickerThumb(@NonNull AdSticker sticker,
                                    @NonNull ImageView imageView) {
        mImageLoader.displayImage(getStickerThumbPath(sticker), imageView);
    }


    /**
     * Load sticker images
     *
     * @param sticker  sticker
     * @param listener listener, image in bitmaps[0], altImage in bitmap[1]
     */
    public void loadStickerImages(@NonNull AdSticker sticker,
                                  @NonNull BitmapLoadListener listener) {
        new StickerImageLoader(sticker, listener).startLoad();
    }

    /**
     * Display scene thumb in imageView
     *
     * @param scene     scene
     * @param imageView imageView
     */
    public void displaySceneThumb(@NonNull Scene scene,
                                  @NonNull ImageView imageView) {
        displayOnlineImage(scene.getImageThumb(), imageView);
    }

    /**
     * Load scene image.
     *
     * @param scene    scene
     * @param listener listener, bitmap in bitmaps[0]
     */
    public void loadSceneImages(@NonNull Scene scene,
                                @NonNull BitmapLoadListener listener) {
        loadOnlineImage(scene.getImage(), listener);
    }

    private String getStickerThumbPath(AdSticker sticker) {
        String path = TextUtils.isEmpty(sticker.getThumb())
                ? sticker.getImage() : sticker.getThumb();
        return getStickerImageDirPath(sticker) + path;
    }

    private String getStickerImagePath(AdSticker sticker) {
        return getStickerImageDirPath(sticker) + sticker.getImage();
    }

    private String getStickerAltImagePath(AdSticker sticker) {
        return getStickerImageDirPath(sticker) + sticker.getAltImage();
    }

    private String getStickerImageDirPath(AdSticker sticker) {
        String path;
        switch (sticker.getFrom()) {
            case INTERNAL:
                path = INTERNAL_STICKER_DIR;
                break;
            case LOCAL:
                path = LOCAL_STICKER_DIR;
                break;
            case ONLINE:
                path = mImageBaseUrl + "/";
                break;
            default:
                path = null;
                break;
        }
        return path;
    }

    /**
     * Display portfolioVO thumb in imageView
     *
     * @param portfolioVO portfolioVO
     * @param imageView   imageView
     */
    public void displayPortfolioThumbImage(@NonNull PortfolioVO portfolioVO,
                                           @NonNull ImageView imageView) {
        displayOnlineImage(portfolioVO.getAdThumbImage(), imageView);
    }

    /**
     * Display portfolioVO thumb in imageView
     *
     * @param portfolioVO     portfolioVO
     * @param imageView       imageView
     * @param defaultImageRes default image res id
     */
    public void displayPortfolioThumbImage(@NonNull PortfolioVO portfolioVO,
                                           @NonNull ImageView imageView,
                                           @DrawableRes int defaultImageRes) {
        displayOnlineImage(portfolioVO.getAdThumbImage(), imageView, defaultImageRes);
    }

    /**
     * Load portfolioVO thumb image.
     *
     * @param portfolioVO portfolioVO
     * @param listener    listener, bitmap in bitmaps[0]
     */
    public void loadPortfolioThumbImage(@NonNull PortfolioVO portfolioVO,
                                        @NonNull BitmapLoadListener listener) {
        loadOnlineImage(portfolioVO.getAdThumbImage(), listener);
    }

    /**
     * Display portfolioVO in imageView
     *
     * @param portfolioVO portfolioVO
     * @param imageView   imageView
     */
    public void displayPortfolioImage(@NonNull PortfolioVO portfolioVO,
                                      @NonNull ImageView imageView) {
        displayOnlineImage(portfolioVO.getAdImage(), imageView);
    }

    /**
     * Display portfolioVO in imageView
     *
     * @param portfolioVO     portfolioVO
     * @param imageView       imageView
     * @param defaultImageRes default image res id
     */
    public void displayPortfolioImage(@NonNull PortfolioVO portfolioVO,
                                      @NonNull ImageView imageView,
                                      @DrawableRes int defaultImageRes) {
        displayOnlineImage(portfolioVO.getAdImage(), imageView, defaultImageRes);
    }

    /**
     * Load portfolioVO image.
     *
     * @param portfolioVO portfolioVO
     * @param listener    listener, bitmap in bitmaps[0]
     */
    public void loadPortfolioImage(@NonNull PortfolioVO portfolioVO,
                                   @NonNull BitmapLoadListener listener) {
        loadOnlineImage(portfolioVO.getAdImage(), listener);
    }

    public void loadPortfolioImage(@NonNull PortfolioVO portfolioVO, Context context, ImageView imageView, int width) {
        loadOnlineImage(portfolioVO.getAdImage(), new CommunityImageLoadListener(context, imageView,width));
    }

    public void loadOnlineImage(@NonNull String shortUrl,
                                @NonNull BitmapLoadListener listener) {
        new BitmapLoader(shortUrl, listener).load();
    }

    public void displayOnlineImage(@NonNull String shortUrl,
                                   @NonNull ImageView imageView) {
        mImageLoader.displayImage(getOnlineImageUrl(shortUrl), imageView);
    }

    public void displayOnlineImage(@NonNull String shortUrl,
                                   @NonNull ImageView imageView, Context context, int width) {
        mImageLoader.displayImage(getOnlineImageUrl(shortUrl), imageView,new SelectHotImageLoadListener(context, imageView, width));
    }

    public void displayOnlineImage(@NonNull String shortUrl,
                                   @NonNull ImageView imageView,
                                   @DrawableRes int defaultImageRes) {
        DisplayOptions options = new DisplayOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .defaultImageRes(defaultImageRes)
                .build();
        mImageLoader.displayImage(getOnlineImageUrl(shortUrl), imageView, options);
    }

    public void displayFixedOnlineImage(@NonNull String url,
                                        @NonNull ImageView imageView, Context context , int width){
        DisplayOptions options = new DisplayOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .build();
        mImageLoader.displayImage(url, imageView, options,new CommunityImageLoadListener(context, imageView,width));
    }

    private String getOnlineImageUrl(@NonNull String shortUrl) {
        return String.format(Locale.ENGLISH, "%1$s/%2$s", mImageBaseUrl, shortUrl);
    }

    private class StickerImageLoader {

        private static final int LOAD_NONE = 0;
        private static final int LOAD_IMAGE = 0x00000001;
        private static final int LOAD_ALT_IMAGE = 0x0000002;

        private final AdSticker mSticker;
        private final BitmapLoadListener mListener;
        private int mLoadFlag;
        private Bitmap[] mBitmaps;

        public StickerImageLoader(AdSticker sticker, BitmapLoadListener listener) {
            mSticker = sticker;
            mListener = listener;
            mLoadFlag = LOAD_NONE;
            mBitmaps = new Bitmap[2];
        }

        public void startLoad() {
            if (mListener == null) {
                return;
            }

            if (!TextUtils.isEmpty(mSticker.getImage())) {
                mLoadFlag |= LOAD_IMAGE;
                mImageLoader.loadImage(getStickerImagePath(mSticker), null, null,
                        new OnlineImageLoadListener() {
                            @Override
                            public void onLoadFailed(String imageUri, LoadFailType loadFailType) {
                                mBitmaps[0] = null;
                                onImageLoaded();
                            }

                            @Override
                            public void onLoadComplete(String imageUri, Bitmap loadedImage) {
                                mBitmaps[0] = loadedImage;
                                onImageLoaded();
                            }
                        });
            }

            if (!TextUtils.isEmpty(mSticker.getAltImage())) {
                mLoadFlag |= LOAD_ALT_IMAGE;
                mImageLoader.loadImage(getStickerAltImagePath(mSticker), null, null,
                        new OnlineImageLoadListener() {
                            @Override
                            public void onLoadFailed(String imageUri, LoadFailType loadFailType) {
                                mBitmaps[1] = null;
                                onAltImageLoaded();
                            }

                            @Override
                            public void onLoadComplete(String imageUri, Bitmap loadedImage) {
                                mBitmaps[1] = loadedImage;
                                onAltImageLoaded();
                            }
                        });
            }
        }

        private void onImageLoaded() {
            mLoadFlag &= ~LOAD_IMAGE;
            notifyLoaded();
        }

        private void onAltImageLoaded() {
            mLoadFlag &= ~LOAD_ALT_IMAGE;
            notifyLoaded();
        }

        private void notifyLoaded() {
            if (mLoadFlag != LOAD_NONE) {
                return;
            }

            boolean success = mBitmaps[0] != null || mBitmaps[1] != null;
            mListener.onLoaded(mSticker, success, mBitmaps);
        }
    }

    private class BitmapLoader {

        private final String mBitmapUrl;
        private final BitmapLoadListener mListener;
        private Bitmap[] mBitmaps;

        public BitmapLoader(String bitmapUrl, BitmapLoadListener listener) {
            mBitmapUrl = bitmapUrl;
            mListener = listener;
            mBitmaps = new Bitmap[1];
        }

        public void load() {
            if (mListener == null) {
                return;
            }

            mImageLoader.loadImage(getOnlineImageUrl(mBitmapUrl), null, null,
                    new OnlineImageLoadListener() {
                        @Override
                        public void onLoadFailed(String imageUri, LoadFailType loadFailType) {
                            mBitmaps[0] = null;
                            onImageLoaded();
                        }

                        @Override
                        public void onLoadComplete(String imageUri, Bitmap loadedImage) {
                            mBitmaps[0] = loadedImage;
                            onImageLoaded();
                        }
                    });
        }

        private void onImageLoaded() {
            notifyLoaded();
        }

        private void notifyLoaded() {
            mListener.onLoaded(mBitmapUrl, mBitmaps[0] != null, mBitmaps);
        }
    }
}
