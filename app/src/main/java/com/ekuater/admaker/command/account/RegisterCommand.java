package com.ekuater.admaker.command.account;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.command.Utils;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
public class RegisterCommand extends BaseCommand {

    public static final String URL = "/services/user/register.json";

    public RegisterCommand() {
        super();
        setUrl(URL);
    }

    public void putParamMobile(String mobile) {
        putParam("mobile", mobile);
    }

    public void putParamNickname(String nickname) {
        putParam("nickName", nickname);
    }

    public void putParamGender(int gender) {
        putParam("sex", gender);
    }

    public void putParamPassword(String password) {
        putParam("password", Utils.encodePassword(password));
    }

    public void putParamCaptcha(String captcha) {
        putParam("captcha", captcha);
    }

    @SuppressWarnings("unused")
    public static class Response extends BaseCommand.Response {

        @SerializedName("admakerCode")
        private String adMakerCode;

        public Response() {
            super();
        }

        public String getAdMakerCode() {
            return adMakerCode;
        }
    }
}
