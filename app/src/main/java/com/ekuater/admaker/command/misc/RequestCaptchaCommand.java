package com.ekuater.admaker.command.misc;

import com.ekuater.admaker.command.BaseCommand;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
public class RequestCaptchaCommand extends BaseCommand {

    public static final String URL = "/services/user/mobile_verify_code.json";

    public static final String SCENARIO_REGISTER = "register";
    public static final String SCENARIO_MODIFY_PASSWORD = "modifyPassword";
    public static final String SCENARIO_BIND_MOBILE = "bindMobile";

    public RequestCaptchaCommand() {
        super();
        setUrl(URL);
    }

    public void putParamMobile(String mobile) {
        putParam("mobile", mobile);
    }

    public void putParamScenario(String scenario) {
        putParam("scenario", scenario);
    }
}
