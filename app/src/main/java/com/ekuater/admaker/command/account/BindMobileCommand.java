package com.ekuater.admaker.command.account;

import com.ekuater.admaker.command.TokenCommand;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
public class BindMobileCommand extends TokenCommand {

    public static final String URL = "/services/third_user/boundMobile.json";

    public BindMobileCommand(String token) {
        super(token);
        setUrl(URL);
    }

    public void putParamOpenId(String openId) {
        putParam("openid", openId);
    }

    public void putParamPlatform(String platform) {
        putParam("platform", platform);
    }

    public void putParamMobile(String mobile) {
        putParam("mobile", mobile);
    }

    public void putParamCaptcha(String captcha) {
        putParam("captcha", captcha);
    }

    public void putParamNewPassword(String newPassword) {
        putParam("newPassWord", newPassword);
    }
}
