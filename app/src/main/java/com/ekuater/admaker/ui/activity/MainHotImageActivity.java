package com.ekuater.admaker.ui.activity;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.datastruct.eventbus.PortfolioChangeEvent;
import com.ekuater.admaker.datastruct.eventbus.PortfolioPublishedEvent;
import com.ekuater.admaker.datastruct.eventbus.UILaunchEvent;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.NormalCallListener;
import com.ekuater.admaker.delegate.PortfolioLoadListener;
import com.ekuater.admaker.delegate.PortfolioManager;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.base.TitleIconActivity;
import com.ekuater.admaker.ui.fragment.LoginDialogFragment;
import com.ekuater.admaker.ui.util.ScreenUtils;
import com.ekuater.admaker.ui.widget.ActionBarDrawerToggle;
import com.ekuater.admaker.ui.widget.ClickEventInterceptLinear;
import com.ekuater.admaker.ui.widget.DrawerArrowDrawable;
import com.ekuater.admaker.util.BmpUtils;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/8/12.
 */
public class MainHotImageActivity extends TitleIconActivity implements View.OnClickListener, Handler.Callback , ViewPagerEx.OnPageChangeListener{

    private static final String TAG = "MainHotImageActivity";

    private static final int LOAD_SUCCESS = 101;
    private static final int LOAD_FAILED = 102;
    private static final int LOAD_PRAISE = 103;

    private LinearLayout mImageArea;
    private ImageView mSpeak;
    private ImageView mPraise;
    private ProgressWheel mProgress;
    private SliderLayout mViewPager;
    private int mPage = 1;

    private PortfolioManager mPortfolioManager;
    private Handler handler = new Handler(this);
    private Context mContext;
    private DisplayMetrics metrics;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private TextView title;
    private EventBus eventBus;
    private boolean isLoadMore;
    private int mViewPageHeight;
    private boolean isContinuePager = false;
    private static final int MAX_COUNT = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_hot_image);
        getSwipeBackLayout().setEnableGesture(false);
        eventBus = UIEventBusHub.getDefaultEventBus();
        eventBus.register(this);
        mContext = this;
        mPortfolioManager = PortfolioManager.getInstance(this);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        initView();
        loadDate();
    }

    private void initTitle() {
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setCustomView(R.layout.custom_actionbar);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
            ab.setDisplayShowCustomEnabled(true);
            title = (TextView) ab.getCustomView().findViewById(R.id.title);
            ab.getCustomView().findViewById(R.id.right_icon).setOnClickListener(this);
        } else {
            title = new TextView(this);
        }
        title.setVisibility(View.VISIBLE);
    }

    private void initView() {
        initTitle();
        mImageArea = (LinearLayout) findViewById(R.id.main_hot_image_area);
        mSpeak = (ImageView) findViewById(R.id.main_hot_image_speak);
        mPraise = (ImageView) findViewById(R.id.main_hot_image_praise);
        mProgress = (ProgressWheel) findViewById(R.id.main_hot_image_progress);
        mViewPager = (SliderLayout) findViewById(R.id.main_hot_image_viewpager);
        mViewPager.addOnPageChangeListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setScrimColor(getResources().getColor(R.color.transparent));
        mSpeak.setOnClickListener(this);
        mPraise.setOnClickListener(this);
        int height = ScreenUtils.getNoStateHeight(this) - BmpUtils.dp2px(mContext, 50);
        mViewPageHeight = (height) - (height/3);
        DrawerArrowDrawable drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        drawerArrow.setColor(R.color.title_color);
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
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        UIEventBusHub.getDefaultEventBus().post(new UILaunchEvent());
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

    private boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_icon:
                UILauncher.launchFeedbackUI(this);
                break;
            case R.id.main_hot_image_speak:
                UILauncher.launchSelectHotImageUI(this);
                break;
            case R.id.main_hot_image_praise:
                UILauncher.launchMainActivity(this);
                break;
            default:
                break;
        }
    }

    private void loadDate() {
        mProgress.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.GONE);
        mPortfolioManager.loadLatestPortfolios(mPage, MAX_COUNT, new PortfolioLoadListener<PortfolioVO>() {
            @Override
            public void onLoaded(boolean success, boolean remaining, PortfolioVO[] dataArray) {
                handler.obtainMessage(success ? LOAD_SUCCESS : LOAD_FAILED, dataArray).sendToTarget();
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handle = true;
        switch (msg.what) {
            case LOAD_SUCCESS:
                onHandlerLoadDataResult(msg);
                break;
            case LOAD_FAILED:
                break;
            case LOAD_PRAISE:
                HotissuesAdapter hotissuesAdapter = (HotissuesAdapter) msg.obj;
                PortfolioVO portfolioVO = hotissuesAdapter.getItem(msg.arg1);
                int praise = portfolioVO.getPraiseNum() + 1;
                hotissuesAdapter.getItem(msg.arg1).setPraiseNum(praise);
                hotissuesAdapter.notifyDataSetChanged();
                break;
            default:
                handle = false;
                break;
        }
        return handle;
    }

    private void onHandlerLoadDataResult(Message msg) {
        PortfolioVO[] portfolioVOs = (PortfolioVO[]) msg.obj;
        if (portfolioVOs != null && portfolioVOs.length > 0) {
            List<PortfolioVO[]> list = new ArrayList<>();
            getPortfolios(portfolioVOs, list);
            isContinuePager = portfolioVOs.length == MAX_COUNT;
            if (mPage == 1){
                addNewsData(list);
            }else{
                addMoreData(list);
            }
            if (!mViewPager.getCycling()){
                mViewPager.startAutoCycle();
            }
            mPage++;
        }
        isLoadMore = false;
        mViewPager.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);
    }

    private void getPortfolios(PortfolioVO[] portfolioVOs, List<PortfolioVO[]> list){
        int count = portfolioVOs.length <= 4 ? 1 : (portfolioVOs.length / 4) + (portfolioVOs.length % 4 == 0 ? 0 : 1);
        int index = 0;
        for (int i = 0; i < count; i++) {
            if (count == 1) {
                list.add(portfolioVOs);
            } else {
                if (index < portfolioVOs.length) {
                    int poorIndex = portfolioVOs.length - index;
                    int childCount = poorIndex <= 4 ? poorIndex : 4;
                    PortfolioVO[] childPortfolioVos = new PortfolioVO[childCount];
                    for (int j = 0; j < childPortfolioVos.length; j++) {
                        childPortfolioVos[j] = portfolioVOs[index];
                        index++;
                    }
                    list.add(childPortfolioVos);
                }
            }
        }
    }

    private void addNewsData(List<PortfolioVO[]> list) {
        List<BaseSliderView> baseSliderViews = new ArrayList<>();
        for (PortfolioVO[] portfolioVOs:list){
            MainGridSliderView gridSliderView = new MainGridSliderView(mContext, mViewPageHeight, sliderLayoutListener);
            gridSliderView.setPortfolios(portfolioVOs);
            baseSliderViews.add(gridSliderView);
        }
        mViewPager.addNewsSlider(baseSliderViews);
    }

    private void addMoreData(List<PortfolioVO[]> list) {
        for (PortfolioVO[] portfolioVOs:list){
            MainGridSliderView gridSliderView = new MainGridSliderView(mContext, mViewPageHeight, sliderLayoutListener);
            gridSliderView.setPortfolios(portfolioVOs);
            mViewPager.addSlider(gridSliderView);
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    @Override
    public void onPageSelected(int position) {
        if (position == mViewPager.getAdapterCount() - 1) {
            if (!isLoadMore && isContinuePager) {
                mViewPager.stopAutoCycle();
                isLoadMore = true;
                loadDate();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private HotissuesAdapter mHotissuesAdapter;

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(PortfolioChangeEvent event) {
        PortfolioVO portfolioVO = event.getPortfolio();
        if (portfolioVO != null && mHotissuesAdapter != null && TAG.equals(event.getTag())) {
            mHotissuesAdapter.getItem(event.getPosition()).setPraiseNum(portfolioVO.getPraiseNum());
            mHotissuesAdapter.notifyDataSetChanged();
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(PortfolioPublishedEvent event) {
        mPage = 1;
        loadDate();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        mViewPager.stopAutoCycle();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventBus != null) {
            eventBus.unregister(this);
        }
    }

    public static class HotissuesAdapter extends BaseAdapter {

        private PortfolioVO[] portfolioVOs;
        private HotissuesAdapter hotissuesAdapter;
        private LayoutInflater inflater;
        private Context context;
        private SliderLayoutListener onclickTest;

        public HotissuesAdapter(Context context,PortfolioVO[] portfolioVOs, SliderLayoutListener onclickTest) {
            hotissuesAdapter = this;
            this.context = context;
            this.portfolioVOs = portfolioVOs;
            inflater = LayoutInflater.from(context);
            this.onclickTest = onclickTest;
        }

        @Override
        public int getCount() {
            return portfolioVOs.length;
        }

        @Override
        public PortfolioVO getItem(int position) {
            return portfolioVOs[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            HotissuesViewHolder holder;
            if (convertView == null) {
                holder = new HotissuesViewHolder();
                convertView = inflater.inflate(R.layout.item_main_hot_image, parent, false);
                holder.cardView = (CardView) convertView.findViewById(R.id.hot_child_relayout);
                holder.imageView = (ImageView) convertView.findViewById(R.id.hot_child_image);
                holder.praise = (ClickEventInterceptLinear) convertView.findViewById(R.id.image_praise);
                holder.praiseNum = (TextView) convertView.findViewById(R.id.image_praise_text);
                holder.praiseImage = (ImageView) convertView.findViewById(R.id.image_praise_image);
                int width = (ScreenUtils.getScreenWidth(context)- BmpUtils.dp2px(context, 30)) / 2;
                float scale = (float) 2 / 3;
                ViewGroup.LayoutParams layoutParams = holder.cardView.getLayoutParams();
                layoutParams.height = (int) (width * scale);
                convertView.setTag(holder);
            } else {
                holder = (HotissuesViewHolder) convertView.getTag();
            }
            final PortfolioVO portfolioVO = getItem(position);
            AdElementDisplay.getInstance(context).displayPortfolioImage(getItem(position), holder.imageView);
            holder.praiseNum.setText(portfolioVO.getPraiseNum() < 1 ? "" : portfolioVO.getPraiseNum() + " ");
            holder.praiseImage.setImageResource(PortfolioManager.getInstance(context).isPortfolioPraised(portfolioVO.getPortfolioId()) ? R.drawable.ic_nice_selected : R.drawable.ic_nice_normal);
            holder.praise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onclickTest.onLoginClick(portfolioVO, position, hotissuesAdapter);
                }
            });
            return convertView;
        }

        public class HotissuesViewHolder {
            private CardView cardView;
            private ImageView imageView;
            private TextView praiseNum;
            private ImageView praiseImage;
            private ClickEventInterceptLinear praise;
        }

        public interface SliderLayoutListener {
            void onLoginClick(PortfolioVO portfolioVO, int position, HotissuesAdapter hotissuesAdapter);

            void onItemClick(HotissuesAdapter hotissuesAdapter, PortfolioVO portfolioVO, int position);
        }
    }

    private HotissuesAdapter.SliderLayoutListener sliderLayoutListener = new HotissuesAdapter.SliderLayoutListener() {
        @Override
        public void onLoginClick(final PortfolioVO portfolioVO, final int position, final HotissuesAdapter hotissuesAdapter) {
            if (isLogin()) {
                praisePortfolio(portfolioVO, position, hotissuesAdapter);
            } else {
                showLoginDialog(new LoginDialogFragment.PushListener() {
                    @Override
                    public void loginFinish() {
                        praisePortfolio(portfolioVO, position, hotissuesAdapter);
                    }
                }, getResources().getString(R.string.login_message_praise));
            }
        }

        @Override
        public void onItemClick(HotissuesAdapter hotissuesAdapter, PortfolioVO portfolioVO, int position) {
            mHotissuesAdapter = hotissuesAdapter;
            UILauncher.launchStoryShowPhotoUI(mContext, portfolioVO, position, TAG);
        }
    };

    private boolean isPraise(PortfolioVO portfolioVO) {
        if (portfolioVO == null) {
            return false;
        }
        return PortfolioManager.getInstance(this).isPortfolioPraised(portfolioVO.getPortfolioId());
    }

    private void praisePortfolio(PortfolioVO portfolioVO, final int position, final Object object) {
        if (!isPraise(portfolioVO)) {
            PortfolioManager.getInstance(this).praisePortfolio(portfolioVO.getPortfolioId(), new NormalCallListener() {
                @Override
                public void onCallResult(boolean success) {
                    if (success) {
                        handler.obtainMessage(LOAD_PRAISE, position, 0, object).sendToTarget();
                    }
                }
            });
        }
    }

}
