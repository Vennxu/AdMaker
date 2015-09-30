package com.ekuater.admaker.command.account;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.command.Utils;
import com.ekuater.admaker.datastruct.UserVO;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
public class LoginCommand extends BaseCommand {

    public static final String URL = "/services/user/login.json";

    public LoginCommand() {
        super();
        setUrl(URL);
    }

    public void putParamLoginText(String loginText) {
        putParam("loginText", loginText);
    }

    public void putParamPassword(String password) {
        putParam("password", Utils.encodePassword(password));
    }

    @SuppressWarnings("unused")
    public static class Response extends BaseCommand.Response {

        private String token;
        private UserVO userVO;

        public Response() {
            super();
        }

        public String getToken() {
            return token;
        }

        public UserVO getUserVO() {
            return userVO;
        }
    }
}
