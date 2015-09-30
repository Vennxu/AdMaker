package com.ekuater.admaker.command.misc;

import com.ekuater.admaker.command.BaseCommand;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
public class VerifyCaptchaCommand extends BaseCommand {

    public static final String URL = "/services/user/confirm_verify_code.json";

    public VerifyCaptchaCommand() {
        super();
        setUrl(URL);
    }

    public void putParamMobile(String mobile) {
        putParam("mobile", mobile);
    }

    public void putParamCaptcha(String captcha) {
        putParam("captcha", captcha);
    }
}
