package com.ekuater.admaker.ui.activity.base;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;

import com.ekuater.admaker.R;
import com.ekuater.admaker.delegate.AccountManager;
import com.ekuater.admaker.ui.fragment.LoginDialogFragment;

/**
 * @author LinYong
 */
public abstract class BackIconActivity extends BaseActivity {


    @Override
    protected void initializeActionBar() {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayUseLogoEnabled(true);
            /*actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);*/
            actionBar.setIcon(R.drawable.lc_ic_ab_back);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                handled = false;
                break;
        }

        return handled || super.onOptionsItemSelected(item);
    }



}
