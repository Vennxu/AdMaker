package com.ekuater.admaker.command.account;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.command.Utils;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
public class ResetPasswordCommand extends BaseCommand {

    public static final String URL = "/services/user/reset_password.json";

    public ResetPasswordCommand() {
        super();
        setUrl(URL);
    }

    public void putParamMobile(String mobile) {
        putParam("mobile", mobile);
    }

    public void putParamCaptcha(String captcha) {
        putParam("captcha", captcha);
    }

    public void putParamNewPassword(String newPassword) {
        putParam("newPassWord", Utils.encodePassword(newPassword));
    }
}
