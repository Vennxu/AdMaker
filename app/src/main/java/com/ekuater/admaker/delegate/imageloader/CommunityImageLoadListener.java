package com.ekuater.admaker.delegate.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ekuater.admaker.delegate.AdElementDisplay;

/**
 * Created by Administrator on 2015/7/14.
 */
public class CommunityImageLoadListener implements OnlineImageLoadListener, AdElementDisplay.BitmapLoadListener {

    private Context mContext;
    private int mWidth;
    private ImageView mImage;

    public CommunityImageLoadListener(Context context, ImageView imageView, int width) {
        mContext = context;
        mWidth = width;
        mImage = imageView;
    }


    @Override
    public void onLoadFailed(String imageUri, LoadFailType loadFailType) {

    }

    @Override
    public void onLoadComplete(String imageUri, Bitmap loadedImage) {
        onLoadImage(loadedImage);
    }

    @Override
    public void onLoaded(Object object, boolean success, Bitmap[] bitmaps) {
        if (success){
            if (bitmaps != null){
                onLoadImage(bitmaps[0]);
            }
        }
    }

    private void onLoadImage(Bitmap loadedImage){
        if (loadedImage != null) {
            if (loadedImage != null) {
                int bmpWidth = loadedImage.getWidth();
                float sale = (float) mWidth / bmpWidth;
                int bmpHeight = (int) (loadedImage.getHeight() * sale);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mImage.getLayoutParams();
                layoutParams.height = bmpHeight;
                layoutParams.width = mWidth;
                mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mImage.setImageBitmap(loadedImage);
            }
        }
    }
}
