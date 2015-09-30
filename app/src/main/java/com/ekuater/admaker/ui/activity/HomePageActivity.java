package com.ekuater.admaker.ui.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.datastruct.SimpleUserVO;
import com.ekuater.admaker.datastruct.eventbus.PortfolioChangeEvent;
import com.ekuater.admaker.datastruct.eventbus.PortfolioPublishedEvent;
import com.ekuater.admaker.delegate.AccountManager;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.NormalCallListener;
import com.ekuater.admaker.delegate.PortfolioLoadListener;
import com.ekuater.admaker.delegate.PortfolioManager;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.fragment.LoginDialogFragment;
import com.ekuater.admaker.ui.fragment.SimpleProgressHelper;
import com.ekuater.admaker.ui.holder.ItemListener;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.ui.widget.DetectTouchGestureLayout;
import com.ekuater.admaker.ui.widget.SwipeRefreshLoadLayout;

import de.greenrobot.event.EventBus;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Administrator on 2015/7/6.
 */
public class HomePageActivity extends BackIconActivity implements View.OnClickListener {

    private static final String TAG = "HomePageActivity";

    public static final int LOAD_USER_PORTFOLIO_SUCCESS = 101;
    public static final int LOAD_USER_PORTFOLIO_FAILED = 102;
    public static final int DELETE_USER_PORTFOLIO_SUCCESS = 103;
    public static final int DELETE_USER_PORTFOLIO_FAILED = 104;
    public static final int PRAISE_USER_PORTFOLIO_SUCCESS = 105;

    public static final String USER = "userVO";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLoadLayout mSwipeRefresh;

    private HomePageAdapter mAdapter;
    private SimpleUserVO mUserVO;
    private int mPage = 1;
    private Context mContext;
    private boolean mIsLoadMore = false;

    private PortfolioManager mPortfolioManager;
    private AdElementDisplay mAdElementDisplay;
    private AccountManager mAccountManager;
    private EventBus mUIEventBus;
    private SimpleProgressHelper mSimpleProgressHelper;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_USER_PORTFOLIO_SUCCESS:
                    onHandlerLoadPortfolioResult(msg);
                    break;
                case LOAD_USER_PORTFOLIO_FAILED:
                    onHandlerLoadFailedResult(getString(R.string.load_failed), R.drawable.emoji_cry);
                    break;
                case DELETE_USER_PORTFOLIO_SUCCESS:
                    mSimpleProgressHelper.dismiss();
                    int position = (int) msg.obj;
                    mAdapter.removeItem(position);
                    onHandlerLoadFailedResult(getString(R.string.delete_success), R.drawable.emoji_smile);
                    UIEventBusHub.getDefaultEventBus().post(new PortfolioPublishedEvent());
                    break;
                case DELETE_USER_PORTFOLIO_FAILED:
                    mSimpleProgressHelper.dismiss();
                    onHandlerLoadFailedResult(getString(R.string.delete_failed), R.drawable.emoji_cry);
                    break;
                case PRAISE_USER_PORTFOLIO_SUCCESS:
                    PortfolioVO portfolioVO = mAdapter.getItem(msg.arg1);
                    int praise = portfolioVO.getPraiseNum() + 1;
                    portfolioVO.setPraiseNum(praise);
                    mAdapter.notifyItemChanged(msg.arg1 + 1);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        mContext = this;
        mUIEventBus = UIEventBusHub.getDefaultEventBus();
        mUIEventBus.register(this);
        mPortfolioManager = PortfolioManager.getInstance(this);
        mAdElementDisplay = AdElementDisplay.getInstance(this);
        mAccountManager = AccountManager.getInstance(this);
        mSimpleProgressHelper = new SimpleProgressHelper(this);
        mUserVO = getIntent() == null ? null : getIntent().<SimpleUserVO>getParcelableExtra(USER);
        initHomePageView();
        initHomePageDate();
        loadUserPortfolio();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUIEventBus != null) {
            mUIEventBus.unregister(this);
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(PortfolioChangeEvent event) {
        PortfolioVO portfolio = event.getPortfolio();
        int position = event.getPosition();
        PortfolioVO itemPortfolioVO = mAdapter.getItem(position);
        if (portfolio != null && itemPortfolioVO != null) {
            itemPortfolioVO.setPraiseNum(portfolio.getPraiseNum());
            mAdapter.notifyDataSetChanged();
        }
    }

    protected void initHomePageView() {
        ((TextView) findViewById(R.id.title)).setText(getString(R.string.home_page));
        mRecyclerView = (RecyclerView) findViewById(R.id.home_page_recycler);
        mSwipeRefresh = (SwipeRefreshLoadLayout) findViewById(R.id.home_page_swipe);
        findViewById(R.id.icon).setOnClickListener(this);
        mSwipeRefresh.setLoadMoreListener(new SwipeRefreshLoadLayout.LoadMoreListener() {
            @Override
            public void loadMore() {
                if (!mIsLoadMore) {
                    mIsLoadMore = true;
                    loadUserPortfolio();
                }
            }
        });
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLoadLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(false);
            }
        });
        ((DetectTouchGestureLayout) findViewById(R.id.gesture)).setSwipeGestureListener(new DetectTouchGestureLayout.onSwipeGestureListener() {
            @Override
            public void onLeftSwipe() {
            }

            @Override
            public void onRightSwipe() {
                finish();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void initHomePageDate() {
        mAdapter = new HomePageAdapter(this, mUserVO, new ItemListener.RecyclerItemListener() {
            @Override
            public void onItemClick(View v, int position) {
                super.onItemClick(v, position);
                PortfolioVO portfolioVO = mAdapter.getItem(position);
                if (portfolioVO != null) {
                    UILauncher.launchStoryShowPhotoUI(mContext, portfolioVO, position, TAG);
                }
            }

            @Override
            public void onDeleteItemClick(View v, int position) {
                if (mUserVO != null) {
                    if (mUserVO.getUserId().equals(mAccountManager.getUserId())) {
                        showMaterialDialog(position);
                    }
                }
            }

            @Override
            public void onPraiseClick(final int position, final PortfolioVO portfolioVO) {
                if (isLogin()) {
                    praisePortfolio(portfolioVO, position);
                } else {
                    showLoginDialog(new LoginDialogFragment.PushListener() {
                        @Override
                        public void loginFinish() {
                            praisePortfolio(portfolioVO, position);
                        }
                    }, getResources().getString(R.string.login_message_praise));
                }
            }
        });
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.isHeader(position) ? gridLayoutManager.getSpanCount() : 1;
            }
        });
        mSwipeRefresh.setColorSchemeResources(R.color.actionBarStyle);
        mSwipeRefresh.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        mSwipeRefresh.setRefreshing(true);
    }

    private void loadUserPortfolio() {
        mPortfolioManager.loadUserPortfolios(mUserVO.getUserId(), mPage, new PortfolioLoadListener<PortfolioVO>() {
            @Override
            public void onLoaded(boolean success, boolean remaining, PortfolioVO[] dataArray) {
                Message message = Message.obtain(handler, success ? LOAD_USER_PORTFOLIO_SUCCESS : LOAD_USER_PORTFOLIO_FAILED, dataArray);
                handler.sendMessage(message);
            }
        });
    }

    private void deleteUserPortfolio(final int positon, PortfolioVO portfolioVO) {
        mSimpleProgressHelper.show();
        mPortfolioManager.deletePortfolio(portfolioVO.getPortfolioId(), new NormalCallListener() {
            @Override
            public void onCallResult(boolean success) {
                Message message = Message.obtain(handler, success ? DELETE_USER_PORTFOLIO_SUCCESS : DELETE_USER_PORTFOLIO_FAILED, positon);
                handler.sendMessage(message);
            }
        });
    }

    private void onHandlerLoadPortfolioResult(Message msg) {
        mSwipeRefresh.setRefreshing(false);
        PortfolioVO[] portfolioVOs = (PortfolioVO[]) msg.obj;
        if (portfolioVOs != null && portfolioVOs.length > 0) {
            mAdapter.addPortfolioVOs(portfolioVOs);
            mPage++;
        }
    }

    private void onHandlerLoadFailedResult(String content, int drawable) {
        if (mContext == null) {
            return;
        }
        ShowToast.makeText(mContext, drawable, content).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.icon:
                finish();
                break;
            default:
                break;
        }
    }

    private void showMaterialDialog(final int position) {
        final MaterialDialog dialog = new MaterialDialog(this);
        dialog.setMessage(getString(R.string.deleted_is_confirm));
        dialog.setPositiveButton(getString(R.string.deleted_confirm), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PortfolioVO portfolioVO = mAdapter.getItem(position);
                deleteUserPortfolio(position, portfolioVO);
            }
        });
        dialog.setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private boolean isPraise(PortfolioVO portfolioVO) {
        if (portfolioVO == null) {
            return false;
        }
        return PortfolioManager.getInstance(this).isPortfolioPraised(portfolioVO.getPortfolioId());
    }

    private void praisePortfolio(PortfolioVO portfolioVO, final int position) {
        if (!isPraise(portfolioVO)) {
            PortfolioManager.getInstance(this).praisePortfolio(portfolioVO.getPortfolioId(), new NormalCallListener() {
                @Override
                public void onCallResult(boolean success) {
                    if (success) {
                        handler.obtainMessage(PRAISE_USER_PORTFOLIO_SUCCESS, position , 0).sendToTarget();
                    }
                }
            });
        }
    }
}
