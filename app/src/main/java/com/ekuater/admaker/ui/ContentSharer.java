package com.ekuater.admaker.ui;

import android.app.Activity;
import android.content.Intent;

import com.ekuater.admaker.R;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;

/**
 * @author LinYong
 */
public final class ContentSharer {

    private static final String DESCRIPTOR = "com.umeng.share";

    private final Activity mActivity;
    private final UMSocialService mController;
    private final ContentShareBoard mShareBoard;

    public ContentSharer(Activity activity) {
        mController = UMServiceFactory.getUMSocialService(DESCRIPTOR);
        mActivity = activity;
        mShareBoard = new ContentShareBoard(mActivity, mController);
        initPlatform();
        initWXSocial();
        initQQSocial();
        initSinaSocial();
    }

    public void setShareContent(ShareContent shareContent) {
        mShareBoard.setShareContent(shareContent);
    }

    public void openSharePanel() {
        mShareBoard.showBoard();
    }

    public void directShareContent(ShareContent shareContent) {
        mShareBoard.directShareContent(shareContent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode);
        if (ssoHandler != null) {
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    private void initPlatform() {
        mController.getConfig().removePlatform(SHARE_MEDIA.TENCENT);
        mController.getConfig().setPlatformOrder(
                SHARE_MEDIA.WEIXIN,
                SHARE_MEDIA.WEIXIN_CIRCLE,
                SHARE_MEDIA.QQ,
                SHARE_MEDIA.QZONE,
                SHARE_MEDIA.SINA);
    }

    private void initWXSocial() {
        final String appId = getString(R.string.weixin_app_id);
        final String appSecret = getString(R.string.weixin_app_secret);

        UMWXHandler wxHandler = new UMWXHandler(mActivity, appId, appSecret);
        wxHandler.addToSocialSDK();
        UMWXHandler wxCircleHandler = new UMWXHandler(mActivity, appId, appSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
    }

    private void initQQSocial() {
        final String appId = getString(R.string.qq_sso_app_id);
        final String appKey = getString(R.string.qq_sso_app_key);

        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(mActivity, appId, appKey);
        qqSsoHandler.addToSocialSDK();
        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(mActivity, appId, appKey);
        qZoneSsoHandler.addToSocialSDK();
    }

    private void initSinaSocial() {
        SinaSsoHandler sinaSsoHandler = new SinaSsoHandler();
        sinaSsoHandler.addToSocialSDK();
    }

    private String getString(int resId) {
        return mActivity.getString(resId);
    }
}
