package com.ekuater.admaker.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.datastruct.eventbus.LoginEvent;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.oauth.OAuthLogin;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.util.TextUtil;

import de.greenrobot.event.EventBus;

/**
 * @author Xu wenxiang
 */
public class LoginDialogFragment extends DialogFragment implements View.OnClickListener, Handler.Callback {

    private static final String TAG = LoginDialogFragment.class.getSimpleName();

    private OAuthLogin mOAuthLoginManager;
    private boolean mInOAuthLogin = false;
    private SimpleProgressDialog mSimpleProgressDialog;
    private EventBus mEventBus;
    private PushListener mPushListener;
    private String mMessage;

    public LoginDialogFragment() {
    }

    public LoginDialogFragment(String message) {
        mMessage = message;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.ShareDialog);
        mSimpleProgressDialog = new SimpleProgressDialog();
        mEventBus = UIEventBusHub.getDefaultEventBus();
        mOAuthLoginManager = new OAuthLogin(getActivity(), null, mOAuthLoginListener);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_dialog_layout, container, false);
        TextView messageTextView = (TextView) view.findViewById(R.id.login_dialog_message);
        view.findViewById(R.id.login_dialog_qq).setOnClickListener(this);
        view.findViewById(R.id.login_dialog_wechat).setOnClickListener(this);
        view.findViewById(R.id.login_dialog_xina).setOnClickListener(this);
        if (!TextUtil.isEmpty(mMessage)) {
            messageTextView.setText(mMessage);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_dialog_qq:
                doOAuthLogin(ConstantCode.OAUTH_PLATFORM_QQ);
                break;
            case R.id.login_dialog_xina:
                doOAuthLogin(ConstantCode.OAUTH_PLATFORM_SINA_WEIBO);
                break;
            case R.id.login_dialog_wechat:
                doOAuthLogin(ConstantCode.OAUTH_PLATFORM_WEIXIN);
                break;
            default:
                break;
        }
    }

    private void doOAuthLogin(String platform) {
        mInOAuthLogin = mOAuthLoginManager.doOAuthLogin(platform);
        if (mInOAuthLogin) {
            showLoginDialog();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
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

    private void updateLoginStatus() {
        if (mPushListener != null) {
            mPushListener.loginFinish();
        }
        mEventBus.post(new LoginEvent());
        dismiss();
    }

    private void showLoginDialog() {
        mSimpleProgressDialog.show(getFragmentManager(), TAG);
    }

    private void dismissLoginDialog() {
        mSimpleProgressDialog.dismiss();
    }

    private void showToast(int iconId, int stringId) {
        Activity activity = getActivity();
        if (activity != null) {
            ShowToast.makeText(activity, iconId, activity.getString(stringId)).show();
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

    public void setPushListener(PushListener pushListener) {
        mPushListener = pushListener;
    }

    public interface PushListener {
        void loginFinish();
    }

}
