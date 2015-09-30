package com.ekuater.admaker.ui.activity;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ekuater.admaker.BuildConfig;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.eventbus.PublishNotLoginEvent;
import com.ekuater.admaker.datastruct.eventbus.UILaunchEvent;
import com.ekuater.admaker.delegate.AccountManager;
import com.ekuater.admaker.settings.Settings;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.activity.base.TitleIconActivity;
import com.ekuater.admaker.ui.fragment.AdvertisementFragment;
import com.ekuater.admaker.ui.fragment.CommunityFragment;
import com.ekuater.admaker.ui.fragment.MainDrawerMenuFragment;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.ui.widget.ActionBarDrawerToggle;
import com.ekuater.admaker.ui.widget.DrawerArrowDrawable;
import com.ekuater.admaker.ui.widget.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/7/1.
 *
 * @author Fan Chong
 */
public class MainActivity extends TitleIconActivity {

    private static final String CLASS_SIMPLE_NAME = MainActivity.class.getSimpleName();
    private static final String NEW_VERSION_KEY = CLASS_SIMPLE_NAME + "_new_version";

    private static final int PAGE_TITLE_ADVERTISEMENT = 0;
    private static final int PAGE_TITLE_COMMUNITY = 1;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView title;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerSlidingTagStrip;
    private AccountManager mAccountManager;
    private Handler mHandler = new Handler();
    private MainDrawerMenuFragment mMenuFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setCustomView(R.layout.custom_actionbar);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
            ab.setDisplayShowCustomEnabled(true);
            title = (TextView) ab.getCustomView().findViewById(R.id.title);
        } else {
            title = new TextView(this);
        }

        UIEventBusHub.getDefaultEventBus().register(this);
        mAccountManager = AccountManager.getInstance(this);
        initView();
        mViewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        mPagerSlidingTagStrip.setViewPager(mViewPager);
        DrawerArrowDrawable drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                drawerArrow, R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case PAGE_TITLE_ADVERTISEMENT:
                        title.setText(getString(R.string.advertisement));
                        break;
                    case PAGE_TITLE_COMMUNITY:
                        title.setText(getString(R.string.community));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mMenuFragment = (MainDrawerMenuFragment) getSupportFragmentManager()
                .findFragmentById(R.id.drawer_left_menu);
    }

    private void initView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setScrimColor(getResources().getColor(R.color.transparent));
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPagerSlidingTagStrip = (PagerSlidingTabStrip) findViewById(R.id.pager_tab);
    }

    private boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(PublishNotLoginEvent event) {
        ShowToast.makeText(this, R.drawable.emoji_sad,
                getString(R.string.login_prompt)).show();
        openDrawer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UIEventBusHub.getDefaultEventBus().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isDrawerOpen()) {
                closeDrawer();
            } else {
                openDrawer();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
        UIEventBusHub.getDefaultEventBus().post(new UILaunchEvent());
        checkNewVersion();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mMenuFragment != null) {
            mMenuFragment.doOAuthActivityResult(requestCode, resultCode, data);
        }
    }

    private void checkNewVersion() {
        ContentResolver cr = getContentResolver();
        if (Settings.Global.getInt(cr, NEW_VERSION_KEY, 0) != BuildConfig.VERSION_CODE) {
            Settings.Global.putInt(cr, NEW_VERSION_KEY, BuildConfig.VERSION_CODE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mAccountManager.isLogin()) {
                        openDrawer();
                    }
                }
            }, 1000);
        }
    }

    private class MainPagerAdapter extends FragmentPagerAdapter
            implements PagerSlidingTabStrip.IconTabProvider {

        private List<Fragment> mPageList = new ArrayList<>();
        private List<String> mTitleList = new ArrayList<>();
        private List<Integer> mIconList = new ArrayList<>();

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
            initFragment();
        }

        private void initFragment() {
            mIconList.add(R.drawable.wsgg);
            mIconList.add(R.drawable.jiepai);
            mTitleList.add(getString(R.string.advertisement));
            mTitleList.add(getString(R.string.community));
            mPageList.add(new AdvertisementFragment());
            mPageList.add(new CommunityFragment());
        }

        @Override
        public Fragment getItem(int position) {
            if (0 <= position && position < mPageList.size()) {
                return mPageList.get(position);
            } else {
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (0 <= position && position < mTitleList.size()) {
                return mTitleList.get(position);
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return mPageList != null ? mPageList.size() : 0;
        }

        @Override
        public int getPageIconResId(int position) {
            return mIconList != null ? mIconList.get(position) : 0;
        }
    }
}
