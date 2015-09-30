package com.ekuater.admaker.delegate;

/**
 * Created by Leo on 2015/6/18.
 *
 * @author LinYong
 */
public interface AdResLoadListener<T> {

    void onLoaded(boolean success, boolean remaining, T[] resArray);
}
