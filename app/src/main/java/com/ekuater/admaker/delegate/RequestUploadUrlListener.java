package com.ekuater.admaker.delegate;

/**
 * Created by Leo on 2015/7/3.
 *
 * @author LinYong
 */
public interface RequestUploadUrlListener {

    void onRequestResult(boolean success, String token, String key);
}
