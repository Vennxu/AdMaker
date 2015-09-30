package com.ekuater.admaker.ui.fragment;

import android.graphics.Bitmap;

/**
 * Created by Leo on 2015/6/1.
 *
 * @author LinYong
 */
public interface AdWorkspaceListener {

    void onBaseImageReady(Bitmap baseImage);

    void onSaveStickersDone(Bitmap savedBitmap);
}
