package com.ekuater.admaker.delegate.imageloader;

import android.graphics.Bitmap;

/**
 * Created by Leo on 2015/1/8.
 *
 * @author LinYong
 */
public interface OnlineImageLoadListener {

    void onLoadFailed(String imageUri, LoadFailType loadFailType);

    void onLoadComplete(String imageUri, Bitmap loadedImage);
}
