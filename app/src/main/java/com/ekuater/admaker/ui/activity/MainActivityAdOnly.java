package com.ekuater.admaker.ui.activity;

import android.os.Bundle;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.eventbus.UILaunchEvent;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.activity.base.TitleIconActivity;

/**
 * Created by Leo on 2015/7/15.
 *
 * @author LinYong
 */
public class MainActivityAdOnly extends TitleIconActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSwipeBackLayout().setEnableGesture(false);
        setContentView(R.layout.activity_main_ad_only);
    }

//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        UIEventBusHub.getDefaultEventBus().post(new UILaunchEvent());
//    }
}
