package com.ekuater.admaker.ui.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Created by Leo on 2015/1/28.
 *
 * @author LinYong
 */
public class SimpleProgressHelper {

    private FragmentManager mFragmentManager;
    private SimpleProgressDialog mProgressDialog;

    public SimpleProgressHelper(Fragment fragment) {
        mFragmentManager = fragment.getFragmentManager();
    }

    public SimpleProgressHelper(FragmentActivity activity) {
        mFragmentManager = activity.getSupportFragmentManager();
    }

    public void show() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(mFragmentManager, "SimpleProgressDialog");
        }
    }

    public void dismiss() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
