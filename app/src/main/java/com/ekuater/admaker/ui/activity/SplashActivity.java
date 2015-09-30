package com.ekuater.admaker.ui.activity;

import android.os.Bundle;
import android.os.Handler;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.eventbus.UILaunchEvent;
import com.ekuater.admaker.delegate.AdResLoader;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Administrator on 2015/6/16.
 *
 * @author FanChong
 */
public class SplashActivity extends BackIconActivity {

    private static final long FINISH_DELAY_TIME = 1000 * 10; // 10 seconds
    private static final long MIN_STAY_DURATION = 800;

    private Handler mHandler;
    private Runnable mLaunchRunnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        MobclickAgent.updateOnlineConfig(this);
        mHandler = new Handler();
        UIEventBusHub.getDefaultEventBus().register(this);
        AdResLoader.getInstance(this).updateResVersion();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                joinMainUI();
            }
        }, MIN_STAY_DURATION);
        mHandler.postDelayed(mLaunchRunnable, FINISH_DELAY_TIME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UIEventBusHub.getDefaultEventBus().unregister(this);
        mHandler.removeCallbacks(mLaunchRunnable);
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(UILaunchEvent event) {
        finish();
    }

    private synchronized void joinMainUI() {
        UILauncher.launchMainSelectHotImageUI(this);
    }
}
