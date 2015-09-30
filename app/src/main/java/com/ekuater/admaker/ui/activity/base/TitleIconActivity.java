package com.ekuater.admaker.ui.activity.base;

import android.app.ActionBar;

import com.ekuater.admaker.R;

/**
 * @author LinYong
 */
public abstract class TitleIconActivity extends BaseActivity {

    @Override
    protected void initializeActionBar() {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            /*actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setIcon(R.drawable.title_icon);
            actionBar.setDisplayShowCustomEnabled(false);*/
            //actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setIcon(R.drawable.empty_title_icon);
        }
    }
}
