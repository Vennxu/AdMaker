package com.ekuater.admaker.ui.oauth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.util.L;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;

import java.util.Map;

/**
 * @author LinYong
 */
public class ThirdOAuth {

    private static final String TAG = ThirdOAuth.class.getSimpleName();

    public static class OAuthInfo {

        public String platform;
        public String openId;
        public String accessToken;
        public String tokenExpire;

        public String nickname;
        public int sex;
        public String avatarUrl;

        public OAuthInfo() {
        }
    }

    public interface IOAuthListener {

        void onOAuthResult(OAuthInfo info, boolean success);

        void onGetOAuthInfoResult(OAuthInfo info, boolean success);
    }

    private static final String DESCRIPTOR = "com.umeng.login";

    private final Activity mActivity;
    private final UMSocialService mController;
    private final IOAuthListener mListener;

    public ThirdOAuth(Activity activity, IOAuthListener listener) {
        mActivity = activity;
        mListener = listener;
        mController = UMServiceFactory.getUMSocialService(DESCRIPTOR);

        initSinaSso();
        initQQSso();
        initWXSso();
    }

    public void doSinaOAuthVerify() {
        mController.doOauthVerify(mActivity, SHARE_MEDIA.SINA,
                new SinaAuthListener());
    }

    public void doQQOAuthVerify() {
        mController.doOauthVerify(mActivity, SHARE_MEDIA.QQ,
                new QQAuthListener());
    }

    public void doWXOAuthVerify() {
        mController.doOauthVerify(mActivity, SHARE_MEDIA.WEIXIN,
                new WXAuthListener());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //使用SSO授权必须添加如下代码
        UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode);
        if (ssoHandler != null) {
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    private void initSinaSso() {
        //设置新浪SSO handler
        mController.getConfig().setSsoHandler(new SinaSsoHandler());
    }

    private void initQQSso() {
        // Set QQ SSO handler
        final String qqAppId = getString(R.string.qq_sso_app_id);
        final String qqAppKey = getString(R.string.qq_sso_app_key);
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(mActivity, qqAppId, qqAppKey);
        mController.getConfig().setSsoHandler(qqSsoHandler);
    }

    private void initWXSso() {
        // Set Weixin SSO handler
        final String appId = getString(R.string.weixin_app_id);
        final String appSecret = getString(R.string.weixin_app_secret);
        UMWXHandler wxHandler = new UMWXHandler(mActivity, appId, appSecret);
        mController.getConfig().setSsoHandler(wxHandler);
    }

    private String getString(int resId) {
        return mActivity.getString(resId);
    }

    private int getSinaGender(String gender) {
        try {
            return (Integer.valueOf(gender) == 0) ? ConstantCode.USER_SEX_FEMALE
                    : ConstantCode.USER_SEX_MALE;
        } catch (Exception e) {
            L.w(TAG, e);
            return ConstantCode.USER_SEX_MALE;
        }
    }

    private int getQQGender(String gender) {
        int sex;

        if (TextUtils.isEmpty(gender)) {
            sex = ConstantCode.USER_SEX_MALE;
        } else if (gender.contains(getString(R.string.male))) {
            sex = ConstantCode.USER_SEX_MALE;
        } else if (gender.contains(getString(R.string.female))) {
            sex = ConstantCode.USER_SEX_FEMALE;
        } else {
            sex = ConstantCode.USER_SEX_MALE;
        }

        return sex;
    }

    private int getWXGender(String gender) {
        try {
            return (Integer.valueOf(gender) == 2) ? ConstantCode.USER_SEX_FEMALE
                    : ConstantCode.USER_SEX_MALE;
        } catch (Exception e) {
            L.w(TAG, e);
            return ConstantCode.USER_SEX_MALE;
        }
    }

    private void notifyOAuthResult(OAuthInfo info, boolean success) {
        if (mListener != null) {
            mListener.onOAuthResult(info, success);
        }
    }

    private void notifyGetOAuthInfoResult(OAuthInfo info, boolean success) {
        if (mListener != null) {
            mListener.onGetOAuthInfoResult(info, success);
        }
    }

    private class SinaAuthListener implements SocializeListeners.UMAuthListener {

        @Override
        public void onStart(SHARE_MEDIA platform) {
        }

        @Override
        public void onComplete(Bundle value, SHARE_MEDIA platform) {
            L.v(TAG, "SinaAuthListener.onComplete()");
            if (value != null) {
                StringBuilder sb = new StringBuilder();
                for (String key : value.keySet()) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(value.get(key));
                    sb.append("\n");
                }
                L.v(TAG, "SinaAuthListener.onComplete(), bundle:\n" + sb.toString());
            }

            OAuthInfo oAuthInfo = new OAuthInfo();
            oAuthInfo.platform = ConstantCode.OAUTH_PLATFORM_SINA_WEIBO;

            if (value != null && !TextUtils.isEmpty(value.getString("uid"))) {
                oAuthInfo.openId = value.getString("uid");
                oAuthInfo.accessToken = value.getString("access_token");
                oAuthInfo.tokenExpire = value.getString("expires_in");
                mController.getPlatformInfo(mActivity, platform, new SinaDataListener(oAuthInfo));
                notifyOAuthResult(oAuthInfo, true);
            } else {
                notifyOAuthResult(oAuthInfo, false);
            }
        }

        @Override
        public void onError(SocializeException e, SHARE_MEDIA platform) {
            OAuthInfo oAuthInfo = new OAuthInfo();
            oAuthInfo.platform = ConstantCode.OAUTH_PLATFORM_SINA_WEIBO;
            notifyOAuthResult(oAuthInfo, false);
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            OAuthInfo oAuthInfo = new OAuthInfo();
            oAuthInfo.platform = ConstantCode.OAUTH_PLATFORM_SINA_WEIBO;
            notifyOAuthResult(oAuthInfo, false);
        }
    }

    private class SinaDataListener implements SocializeListeners.UMDataListener {

        private final OAuthInfo mOAuthInfo;

        public SinaDataListener(OAuthInfo oAuthInfo) {
            mOAuthInfo = oAuthInfo;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onComplete(int status, Map<String, Object> info) {
            L.v(TAG, "SinaDataListener.onComplete(), state=%1$d", status);
            if (info != null) {
                StringBuilder sb = new StringBuilder();
                for (String key : info.keySet()) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(info.get(key));
                    sb.append("\n");
                }
                L.v(TAG, "SinaDataListener.onComplete(), info:\n" + sb.toString());
            }

            boolean success = false;

            if (status == 200 && info != null) {
                // success
                try {
                    mOAuthInfo.nickname = info.get("screen_name").toString();
                    mOAuthInfo.avatarUrl = info.get("profile_image_url").toString();
                    mOAuthInfo.sex = getSinaGender(info.get("gender").toString());
                    if (TextUtils.isEmpty(mOAuthInfo.accessToken)) {
                        mOAuthInfo.accessToken = info.get("access_token").toString();
                    }
                    success = !TextUtils.isEmpty(mOAuthInfo.accessToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            notifyGetOAuthInfoResult(mOAuthInfo, success);
        }
    }

    private class QQAuthListener implements SocializeListeners.UMAuthListener {

        @Override
        public void onStart(SHARE_MEDIA platform) {
        }

        @Override
        public void onComplete(Bundle value, SHARE_MEDIA platform) {
            L.v(TAG, "QQAuthListener.onComplete()");
            if (value != null) {
                StringBuilder sb = new StringBuilder();
                for (String key : value.keySet()) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(value.get(key));
                    sb.append("\n");
                }
                L.v(TAG, "QQAuthListener.onComplete(), bundle:\n" + sb.toString());
            }

            OAuthInfo oAuthInfo = new OAuthInfo();
            oAuthInfo.platform = ConstantCode.OAUTH_PLATFORM_QQ;

            if (value != null && !TextUtils.isEmpty(value.getString("uid"))) {
                oAuthInfo.openId = value.getString("uid");
                oAuthInfo.accessToken = value.getString("access_token");
                oAuthInfo.tokenExpire = value.getString("expires_in");
                mController.getPlatformInfo(mActivity, platform, new QQDataListener(oAuthInfo));
                notifyOAuthResult(oAuthInfo, true);
            } else {
                notifyOAuthResult(oAuthInfo, false);
            }
        }

        @Override
        public void onError(SocializeException e, SHARE_MEDIA platform) {
            OAuthInfo oAuthInfo = new OAuthInfo();
            oAuthInfo.platform = ConstantCode.OAUTH_PLATFORM_QQ;
            notifyOAuthResult(oAuthInfo, false);
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            OAuthInfo oAuthInfo = new OAuthInfo();
            oAuthInfo.platform = ConstantCode.OAUTH_PLATFORM_QQ;
            notifyOAuthResult(oAuthInfo, false);
        }
    }

    private class QQDataListener implements SocializeListeners.UMDataListener {

        private final OAuthInfo mOAuthInfo;

        public QQDataListener(OAuthInfo oAuthInfo) {
            mOAuthInfo = oAuthInfo;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onComplete(int status, Map<String, Object> info) {
            L.v(TAG, "QQDataListener.onComplete(), state=%1$d", status);
            if (info != null) {
                StringBuilder sb = new StringBuilder();
                for (String key : info.keySet()) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(info.get(key));
                    sb.append("\n");
                }
                L.v(TAG, "QQDataListener.onComplete(), info:\n" + sb.toString());
            }

            boolean success = false;

            if (status == 200 && info != null) {
                // success
                try {
                    mOAuthInfo.nickname = info.get("screen_name").toString();
                    mOAuthInfo.avatarUrl = info.get("profile_image_url").toString();
                    mOAuthInfo.sex = getQQGender(info.get("gender").toString());
                    success = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            notifyGetOAuthInfoResult(mOAuthInfo, success);
        }
    }

    private class WXAuthListener implements SocializeListeners.UMAuthListener {

        @Override
        public void onStart(SHARE_MEDIA platform) {
        }

        @Override
        public void onComplete(Bundle value, SHARE_MEDIA platform) {
            L.v(TAG, "WXAuthListener.onComplete()");
            if (value != null) {
                StringBuilder sb = new StringBuilder();
                for (String key : value.keySet()) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(value.get(key));
                    sb.append("\n");
                }
                L.v(TAG, "WXAuthListener.onComplete(), bundle:\n" + sb.toString());
            }

            OAuthInfo oAuthInfo = new OAuthInfo();
            oAuthInfo.platform = ConstantCode.OAUTH_PLATFORM_WEIXIN;

            if (value != null && !TextUtils.isEmpty(value.getString("openid"))) {
                oAuthInfo.openId = value.getString("openid");
                oAuthInfo.accessToken = value.getString("access_token");
                oAuthInfo.tokenExpire = value.getString("expires_in");
                mController.getPlatformInfo(mActivity, platform, new WXDataListener(oAuthInfo));
                notifyOAuthResult(oAuthInfo, true);
            } else {
                notifyOAuthResult(oAuthInfo, false);
            }
        }

        @Override
        public void onError(SocializeException e, SHARE_MEDIA platform) {
            OAuthInfo oAuthInfo = new OAuthInfo();
            oAuthInfo.platform = ConstantCode.OAUTH_PLATFORM_WEIXIN;
            notifyOAuthResult(oAuthInfo, false);
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            OAuthInfo oAuthInfo = new OAuthInfo();
            oAuthInfo.platform = ConstantCode.OAUTH_PLATFORM_WEIXIN;
            notifyOAuthResult(oAuthInfo, false);
        }
    }

    private class WXDataListener implements SocializeListeners.UMDataListener {

        private final OAuthInfo mOAuthInfo;

        public WXDataListener(OAuthInfo oAuthInfo) {
            mOAuthInfo = oAuthInfo;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onComplete(int status, Map<String, Object> info) {
            L.v(TAG, "WXDataListener.onComplete(), state=%1$d", status);
            if (info != null) {
                StringBuilder sb = new StringBuilder();
                for (String key : info.keySet()) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(info.get(key));
                    sb.append("\n");
                }
                L.v(TAG, "WXDataListener.onComplete(), info:\n" + sb.toString());
            }

            boolean success = false;

            if (status == 200 && info != null) {
                // success
                try {
                    mOAuthInfo.nickname = info.get("nickname").toString();
                    mOAuthInfo.avatarUrl = info.get("headimgurl").toString();
                    mOAuthInfo.sex = getWXGender(info.get("sex").toString());
                    success = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            notifyGetOAuthInfoResult(mOAuthInfo, success);
        }
    }
}
