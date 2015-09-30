package com.ekuater.admaker;

import android.app.Application;

/**
 * Created by Leo on 2015/8/19.
 *
 * @author Leo
 */
public class TheApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        EnvConfig.init(getApplicationContext());
    }
}
