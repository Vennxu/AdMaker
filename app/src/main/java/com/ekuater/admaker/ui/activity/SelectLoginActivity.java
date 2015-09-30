package com.ekuater.admaker.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.datastruct.eventbus.UILaunchEvent;
import com.ekuater.admaker.delegate.AccountManager;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.fragment.SimpleProgressDialog;
import com.ekuater.admaker.ui.oauth.OAuthLogin;
import com.ekuater.admaker.ui.util.ShowToast;

/**
 * Created by Administrator on 2015/7/1.
 *
 * @author Xu Wenxiang
 */
public class SelectLoginActivity extends BackIconActivity implements View.OnClickListener {

    private static final String LOGIN_DIALOG_TAG = "LoginDialog";

    private ImageView mLoginSina;
    private ImageView mLoginWeixin;
    private ImageView mLoginQQ;

    private OAuthLogin mOAuthLoginManager;
    private boolean mInOAuthLogin = false;
    private SimpleProgressDialog mLoginDialog;
    private AccountManager mAccountManager;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_login);
        mAccountManager = AccountManager.getInstance(this);
        mOAuthLoginManager = new OAuthLogin(this, getMainLooper(), mOAuthLoginListener);
        mInOAuthLogin = false;
        initView();
    }

    private void initView() {

        mLoginQQ = (ImageView) findViewById(R.id.select_qq);
        mLoginWeixin = (ImageView) findViewById(R.id.select_weixin);
        mLoginSina = (ImageView) findViewById(R.id.select_sina);
        mLoginQQ.setOnClickListener(this);
        mLoginSina.setOnClickListener(this);
        mLoginWeixin.setOnClickListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        UIEventBusHub.getDefaultEventBus().post(new UILaunchEvent());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_qq:
                doOAuthLogin(ConstantCode.OAUTH_PLATFORM_QQ);
                break;
            case R.id.select_sina:
                doOAuthLogin(ConstantCode.OAUTH_PLATFORM_SINA_WEIBO);
                break;
            case R.id.select_weixin:
                doOAuthLogin(ConstantCode.OAUTH_PLATFORM_WEIXIN);
                break;
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
            ShowToast.makeText(this, R.drawable.emoji_cry,
                    getString(R.string.third_platform_oauth_failure)).show();
        }
    }

    private void handleGetOAuthInfoResult(boolean success) {
        if (!success) {
            mInOAuthLogin = false;
            dismissLoginDialog();
            ShowToast.makeText(this, R.drawable.emoji_cry,
                    getString(R.string.third_platform_oauth_failure)).show();
        }
    }

    private void handleOAuthLoginResult(int result) {
        mInOAuthLogin = false;
        dismissLoginDialog();

        switch (result) {
            case 1:
                //TODO
                break;
            default:
                ShowToast.makeText(this, R.drawable.emoji_cry,
                        getString(R.string.login_failure)).show();
                break;
        }
    }

    private void showLoginDialog() {
        if (mLoginDialog == null) {
            mLoginDialog = SimpleProgressDialog.newInstance();
            mLoginDialog.show(getSupportFragmentManager(), LOGIN_DIALOG_TAG);
        }
    }

    private void dismissLoginDialog() {
        if (mLoginDialog != null) {
            mLoginDialog.dismiss();
            mLoginDialog = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mOAuthLoginManager.onActivityResult(requestCode, resultCode, data);
    }
}
