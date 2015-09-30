package com.ekuater.admaker.delegate;

/**
 * Created by Leo on 2015/7/3.
 *
 * @author LinYong
 */
public interface UploadListener {

    void onProgress(double percent);

    void onComplete(boolean success, String response);
}
