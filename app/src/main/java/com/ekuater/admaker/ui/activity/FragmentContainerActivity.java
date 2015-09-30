package com.ekuater.admaker.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.ekuater.admaker.R;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;

/**
 * @author Linyong
 */
public class FragmentContainerActivity extends BackIconActivity {

    public static final String EXTRA_SHOW_FRAGMENT = ":android:show_fragment";
    public static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":android:show_fragment_args";

    private String mFragmentClass;
    private Bundle mFragmentArguments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        parseFragmentAndArguments();
        showFragment();
    }

    private void parseFragmentAndArguments() {
        final Intent intent = getIntent();
        mFragmentClass = intent.getStringExtra(EXTRA_SHOW_FRAGMENT);
        mFragmentArguments = intent.getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS);
    }

    private void showFragment() {
        Fragment fragment = Fragment.instantiate(this, mFragmentClass, mFragmentArguments);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, mFragmentClass);
        transaction.commitAllowingStateLoss();
    }
}
