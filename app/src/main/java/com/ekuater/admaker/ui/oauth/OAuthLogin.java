package com.ekuater.admaker.ui.oauth;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.delegate.AccountManager;
import com.ekuater.admaker.delegate.NormalCallListener;
import com.ekuater.admaker.util.L;

/**
 * Created by Leo on 2014/12/24.
 *
 * @author LinYong
 */
public class OAuthLogin {

    private static final String TAG = OAuthLogin.class.getSimpleName();

    private static final int MSG_HANDLE_OAUTH_LOGIN_RESULT = 101;
    private static final int MSG_HANDLE_OAUTH_VERIFY_RESULT = 102;
    private static final int MSG_HANDLE_GET_OAUTH_INFO_RESULT = 103;

    public interface LoginListener {

        void onOAuthVerifyResult(boolean success);

        void onGetOAuthInfoResult(boolean success);

        void onOAuthLoginResult(int result);
    }

    private interface ListenerNotifier {
        void notify(LoginListener listener);
    }

    private static class OAuthResult {

        public final ThirdOAuth.OAuthInfo oAuthInfo;
        public final boolean success;

        public OAuthResult(ThirdOAuth.OAuthInfo info, boolean success) {
            this.oAuthInfo = info;
            this.success = success;
        }
    }

    private class MainHandler extends Handler {

        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_OAUTH_LOGIN_RESULT:
                    handleOAuthLoginResult(msg.arg1);
                    break;
                case MSG_HANDLE_OAUTH_VERIFY_RESULT:
                    handleOAuthVerifyResult((OAuthResult) msg.obj);
                    break;
                case MSG_HANDLE_GET_OAUTH_INFO_RESULT:
                    handleGetOAuthInfoResult((OAuthResult) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private final LoginListener mListener;
    private final ThirdOAuth mThirdOAuth;
    private final AccountManager mAccountManager;
    private final Handler mHandler;
    private final NormalCallListener normalCallListener = new NormalCallListener() {

        @Override
        public void onCallResult(boolean success) {
            Message message = mHandler.obtainMessage(MSG_HANDLE_OAUTH_LOGIN_RESULT, success ? 1 : 0, 0);
            mHandler.sendMessage(message);
        }
    };

    public OAuthLogin(Activity activity, Looper looper, LoginListener listener) {
        if (activity == null) {
            throw new NullPointerException("OAuthLoginManager empty activity");
        }
        if (listener == null) {
            throw new NullPointerException("OAuthLoginManager empty listener");
        }

        if (looper == null) {
            looper = activity.getMainLooper();
        }

        mListener = listener;
        mHandler = new MainHandler(looper);
        mThirdOAuth = new ThirdOAuth(activity, new ThirdOAuth.IOAuthListener() {
            @Override
            public void onOAuthResult(ThirdOAuth.OAuthInfo info, boolean success) {
                Message message = mHandler.obtainMessage(MSG_HANDLE_OAUTH_VERIFY_RESULT,
                        new OAuthResult(info, success));
                mHandler.sendMessage(message);
            }

            @Override
            public void onGetOAuthInfoResult(ThirdOAuth.OAuthInfo info, boolean success) {
                Message message = mHandler.obtainMessage(MSG_HANDLE_GET_OAUTH_INFO_RESULT,
                        new OAuthResult(info, success));
                mHandler.sendMessage(message);
            }
        });
        mAccountManager = AccountManager.getInstance(activity);
    }

    public void onDestroy() {
    }

    public boolean doOAuthLogin(String platform) {
        L.v(TAG, "doOAuthLogin(), platform=" + platform);

        boolean _ret = true;

        if (ConstantCode.OAUTH_PLATFORM_QQ.equals(platform)) {
            mThirdOAuth.doQQOAuthVerify();
        } else if (ConstantCode.OAUTH_PLATFORM_SINA_WEIBO.equals(platform)) {
            mThirdOAuth.doSinaOAuthVerify();
        } else if (ConstantCode.OAUTH_PLATFORM_WEIXIN.equals(platform)) {
            mThirdOAuth.doWXOAuthVerify();
        } else {
            _ret = false;
        }
        return _ret;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mThirdOAuth.onActivityResult(requestCode, resultCode, data);
    }

    private void handleOAuthVerifyResult(OAuthResult oAuthResult) {
        L.v(TAG, "handleOAuthVerifyResult(), success=" + oAuthResult.success);
        notifyOAuthVerifyResult(oAuthResult.success);
    }

    private void handleGetOAuthInfoResult(OAuthResult oAuthResult) {
        L.v(TAG, "handleGetOAuthInfoResult(), success=" + oAuthResult.success);
        if (oAuthResult.success) {
            oAuthLoginInternal(oAuthResult.oAuthInfo);
        }
        notifyGetOAuthInfoResult(oAuthResult.success);
    }

    private void oAuthLoginInternal(ThirdOAuth.OAuthInfo oAuthInfo) {
        L.v(TAG, "oAuthLoginInternal(), start third platform login");
        mAccountManager.thirdLogin(oAuthInfo.platform, oAuthInfo.openId,
                oAuthInfo.accessToken, oAuthInfo.tokenExpire, normalCallListener);
    }

    private void handleOAuthLoginResult(int result) {
        notifyOAuthLoginResult(result);
    }

    private void notifyListener(ListenerNotifier notifier) {
        notifier.notify(mListener);
    }

    private void notifyOAuthVerifyResult(boolean success) {
        notifyListener(new OAuthVerifyResultNotifier(success));
    }

    private void notifyGetOAuthInfoResult(boolean success) {
        notifyListener(new GetOAuthInfoResultNotifier(success));
    }

    private void notifyOAuthLoginResult(int result) {
        notifyListener(new OAuthLoginResultNotifier(result));
    }

    private static class OAuthVerifyResultNotifier implements ListenerNotifier {

        private final boolean mSuccess;

        public OAuthVerifyResultNotifier(boolean success) {
            mSuccess = success;
        }

        @Override
        public void notify(LoginListener listener) {
            listener.onOAuthVerifyResult(mSuccess);
        }
    }

    private static class GetOAuthInfoResultNotifier implements ListenerNotifier {

        private final boolean mSuccess;

        public GetOAuthInfoResultNotifier(boolean success) {
            mSuccess = success;
        }

        @Override
        public void notify(LoginListener listener) {
            listener.onGetOAuthInfoResult(mSuccess);
        }
    }

    private static class OAuthLoginResultNotifier implements ListenerNotifier {

        private final int mResult;

        public OAuthLoginResultNotifier(int result) {
            mResult = result;
        }

        @Override
        public void notify(LoginListener listener) {
            listener.onOAuthLoginResult(mResult);
        }
    }
}
