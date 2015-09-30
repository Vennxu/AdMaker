package com.ekuater.admaker.delegate;

/**
 * Created by Leo on 2015/7/2.
 *
 * @author LinYong
 */
public interface PortfolioLoadListener<T> {

    void onLoaded(boolean success, boolean remaining, T[] dataArray);
}
