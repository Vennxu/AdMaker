package com.ekuater.admaker.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.datastruct.UserVO;
import com.ekuater.admaker.datastruct.UserVOUtils;
import com.ekuater.admaker.datastruct.eventbus.LoginEvent;
import com.ekuater.admaker.datastruct.eventbus.PortfolioChangeEvent;
import com.ekuater.admaker.datastruct.eventbus.PortfolioPublishedEvent;
import com.ekuater.admaker.delegate.AccountManager;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.NormalCallListener;
import com.ekuater.admaker.delegate.PortfolioLoadListener;
import com.ekuater.admaker.delegate.PortfolioManager;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.base.BaseActivity;
import com.ekuater.admaker.ui.oauth.OAuthLogin;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.ui.widget.CircleImageView;

import java.util.Arrays;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/7/15.
 *
 * @author LinYong
 */
public class MainDrawerMenuFragment extends Fragment
        implements View.OnClickListener, Handler.Callback {

    private static final String TAG = "MainDrawerMenuFragment";

    private static final int MAX_PORTFOLIO_PREVIEW_COUNT = 6;
    private static final int MSG_LOAD_PREVIEW_WORKS = 101;
    private static final int LOAD_PRAISE = 102;

    private Activity mActivity;
    private Handler mHandler;
    private PreviewWorksAdapter mAdapter;
    private PortfolioManager mPortfolioManager;
    private AccountManager mAccountManager;
    private AdElementDisplay mAdElementDisplay;
    private OAuthLogin mOAuthLoginManager;
    private SimpleProgressHelper mSimpleProgressHelper;
    private boolean mInOAuthLogin = false;

    private CircleImageView mUserAvatar;
    private TextView mUserNickname;
    private ImageView mUserGender;
    private RelativeLayout mShowLoginArea;
    private ProgressBar mLoadPreview;
    private FrameLayout mShowPreviewArea;
    private BaseActivity baseActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mHandler = new Handler(this);
        baseActivity = (BaseActivity) getActivity();
        mAdapter = new PreviewWorksAdapter(mActivity);
        mPortfolioManager = PortfolioManager.getInstance(mActivity);
        mAccountManager = AccountManager.getInstance(mActivity);
        mAdElementDisplay = AdElementDisplay.getInstance(mActivity);
        mOAuthLoginManager = new OAuthLogin(mActivity, null, mOAuthLoginListener);
        mSimpleProgressHelper = new SimpleProgressHelper(this);
        UIEventBusHub.getDefaultEventBus().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_left_menu, container, false);

        mUserAvatar = (CircleImageView) rootView.findViewById(R.id.user_avatar);
        mUserNickname = (TextView) rootView.findViewById(R.id.user_nickname);
        mUserGender = (ImageView) rootView.findViewById(R.id.user_gender);
        mShowLoginArea = (RelativeLayout) rootView.findViewById(R.id.show_login);
        rootView.findViewById(R.id.icon_qq).setOnClickListener(this);
        rootView.findViewById(R.id.icon_weibo).setOnClickListener(this);
        rootView.findViewById(R.id.weixin_login_area).setOnClickListener(this);
        mUserAvatar.setOnClickListener(this);
        mLoadPreview = (ProgressBar) rootView.findViewById(R.id.load_preview);
        rootView.findViewById(R.id.click_area).setOnClickListener(this);
        mShowPreviewArea = (FrameLayout) rootView.findViewById(R.id.show_preview_works_area);
        final GridView dv = (GridView) rootView.findViewById(R.id.grid_view);
        dv.setAdapter(mAdapter);
        mAdapter.setPreviewWorksClickListener(new PreviewWorksAdapter.PreviewWorksClickListener() {
            @Override
            public void onPraiseClick(final PortfolioVO portfolioVO, final int position) {
                if (baseActivity.isLogin()) {
                    praisePortfolio(portfolioVO, position);
                }else{
                    baseActivity.showLoginDialog(new LoginDialogFragment.PushListener() {
                        @Override
                        public void loginFinish() {
                            praisePortfolio(portfolioVO, position);
                        }
                    }, getResources().getString(R.string.login_message_praise));
                }
            }
        });
        dv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PortfolioVO portfolioVO = mAdapter.getItem(position);
                if (portfolioVO != null) {
                    UILauncher.launchStoryShowPhotoUI(mActivity, portfolioVO, position, TAG);
                }
            }
        });
        rootView.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        View rootView = getView();
        int width;

        wm.getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels - (dm.widthPixels / 4);
        mAdapter.setWidth(width);
        if (rootView != null) {
            ViewGroup.LayoutParams lp = rootView.getLayoutParams();
            lp.width = width;
            rootView.setLayoutParams(lp);
        }

        updateLoginStatus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UIEventBusHub.getDefaultEventBus().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        UserVO userVO = mAccountManager.getUserVO();

        switch (v.getId()) {
            case R.id.icon_qq:
                doOAuthLogin(ConstantCode.OAUTH_PLATFORM_QQ);
                break;
            case R.id.icon_weibo:
                doOAuthLogin(ConstantCode.OAUTH_PLATFORM_SINA_WEIBO);
                break;
            case R.id.weixin_login_area:
                doOAuthLogin(ConstantCode.OAUTH_PLATFORM_WEIXIN);
                break;
            case R.id.user_avatar:
                if (userVO != null) {
                    UILauncher.launchHomePageUI(mActivity, UserVOUtils.toSimpleUserVO(userVO));
                }
                break;
            case R.id.click_area:
                if (userVO != null) {
                    UILauncher.launchHomePageUI(mActivity, UserVOUtils.toSimpleUserVO(userVO));
                }
                break;
            default:
                break;
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(PortfolioPublishedEvent event) {
        loadPreviewWorks();
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(PortfolioChangeEvent event) {
        PortfolioVO portfolioVO = event.getPortfolio();
        if (portfolioVO != null && TAG.equals(event.getTag())){
            mAdapter.getItem(event.getPosition()).setPraiseNum(portfolioVO.getPraiseNum());
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(LoginEvent event) {
        updateLoginStatus();
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_LOAD_PREVIEW_WORKS:
                handlerPreviewWorksResult(msg.arg1 != 0, (PortfolioVO[]) msg.obj);
                break;
            case LOAD_PRAISE:
                PortfolioVO portfolioVO = mAdapter.getItem(msg.arg1);
                int praise = portfolioVO.getPraiseNum() + 1;
                mAdapter.getItem(msg.arg1).setPraiseNum(praise);
                mAdapter.notifyDataSetChanged();
                break;
            default:
                handled = false;
        }
        return handled;
    }

    public void doOAuthActivityResult(int requestCode, int resultCode, Intent data) {
        mOAuthLoginManager.onActivityResult(requestCode, resultCode, data);
    }

    private void loadPreviewWorks() {
        mLoadPreview.setVisibility(View.VISIBLE);
        mPortfolioManager.loadUserPortfolios(null, 1, new PortfolioLoadListener<PortfolioVO>() {
            @Override
            public void onLoaded(boolean success, boolean remaining, PortfolioVO[] dataArray) {
                mHandler.obtainMessage(MSG_LOAD_PREVIEW_WORKS, success ? 1 : 0,
                        0, dataArray).sendToTarget();
            }
        });
    }

    private void handlerPreviewWorksResult(boolean success, final PortfolioVO[] portfolioVOs) {
        if (success) {
            mLoadPreview.setVisibility(View.GONE);
            mAdapter.updatePortfolios(filterPreviewPortfolios(portfolioVOs));
        }
    }

    private PortfolioVO[] filterPreviewPortfolios(PortfolioVO[] portfolioVOs) {
        return (portfolioVOs != null && portfolioVOs.length > MAX_PORTFOLIO_PREVIEW_COUNT)
                ? Arrays.copyOf(portfolioVOs, MAX_PORTFOLIO_PREVIEW_COUNT) : portfolioVOs;
    }

    private void updateLoginStatus() {
        if (mAccountManager.isLogin()) {
            loadPreviewWorks();
            mShowLoginArea.setVisibility(View.GONE);
            mShowPreviewArea.setVisibility(View.VISIBLE);
            UserVO userVO = mAccountManager.getUserVO();
            if (userVO != null) {
                mAdElementDisplay.displayOnlineImage(userVO.getAvatarThumb(), mUserAvatar);
                mUserNickname.setText(userVO.getNickname());
                mUserGender.setImageResource(ConstantCode.getSexImageResource(userVO.getGender()));
            }
        } else {
            mShowLoginArea.setVisibility(View.VISIBLE);
            mShowPreviewArea.setVisibility(View.GONE);
        }
    }

    private void doOAuthLogin(String platform) {
        mInOAuthLogin = mOAuthLoginManager.doOAuthLogin(platform);
        if (mInOAuthLogin) {
            showLoginDialog();
        }
    }

    private void handleOAuthVerifyResult(boolean success) {
        if (!success) {
            mInOAuthLogin = false;
            dismissLoginDialog();
            showToast(R.drawable.emoji_cry, R.string.third_platform_oauth_failure);
        }
    }

    private void handleGetOAuthInfoResult(boolean success) {
        if (!success) {
            mInOAuthLogin = false;
            dismissLoginDialog();
            showToast(R.drawable.emoji_cry, R.string.third_platform_oauth_failure);
        }
    }

    private void handleOAuthLoginResult(int result) {
        mInOAuthLogin = false;
        dismissLoginDialog();

        switch (result) {
            case 1:
                updateLoginStatus();
                break;
            default:
                showToast(R.drawable.emoji_cry, R.string.login_failure);
                break;
        }
    }

    private OAuthLogin.LoginListener mOAuthLoginListener
            = new OAuthLogin.LoginListener() {

        @Override
        public void onOAuthVerifyResult(boolean success) {
            handleOAuthVerifyResult(success);
        }

        @Override
        public void onGetOAuthInfoResult(boolean success) {
            handleGetOAuthInfoResult(success);
        }

        @Override
        public void onOAuthLoginResult(int result) {
            handleOAuthLoginResult(result);
        }
    };

    private void showLoginDialog() {
        mSimpleProgressHelper.show();
    }

    private void dismissLoginDialog() {
        mSimpleProgressHelper.dismiss();
    }

    private void showToast(int iconId, int stringId) {
        showToast(iconId, mActivity.getString(stringId));
    }

    private void showToast(int iconId, CharSequence text) {
        ShowToast.makeText(mActivity, iconId, text).show();
    }

    private boolean isPraise(PortfolioVO portfolioVO){
        if (portfolioVO == null){
            return false;
        }
        return PortfolioManager.getInstance(getActivity()).isPortfolioPraised(portfolioVO.getPortfolioId());
    }

    private void praisePortfolio(PortfolioVO portfolioVO, final int position){
        if (!isPraise(portfolioVO)) {
            PortfolioManager.getInstance(getActivity()).praisePortfolio(portfolioVO.getPortfolioId(), new NormalCallListener() {
                @Override
                public void onCallResult(boolean success) {
                    if (success) {
                        mHandler.obtainMessage(LOAD_PRAISE, position, 0).sendToTarget();
                    }
                }
            });
        }
    }
}
