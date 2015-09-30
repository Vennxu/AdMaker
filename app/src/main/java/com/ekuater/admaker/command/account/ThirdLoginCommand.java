package com.ekuater.admaker.command.account;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.datastruct.UserVO;

/**
 * Created by Leo on 2015/7/1.
 *
 * @author LinYong
 */
public class ThirdLoginCommand extends BaseCommand {

    public static final String URL = "/services/third_user/login.json";

    public ThirdLoginCommand() {
        super();
        setUrl(URL);
    }

    public void putParamOpenId(String openId) {
        putParam("openid", openId);
    }

    public void putParamAccessToken(String accessToken) {
        putParam("accessToken", accessToken);
    }

    public void putParamTokenExpire(String tokenExpire) {
        putParam("tokenExpirein", tokenExpire);
    }

    public void putParamPlatform(String platform) {
        putParam("platform", platform);
    }

    @SuppressWarnings("unused")
    public static class Response extends BaseCommand.Response {

        private String token;
        private UserVO userVO;
        private String password;

        public Response() {
            super();
        }

        public String getToken() {
            return token;
        }

        public UserVO getUserVO() {
            return userVO;
        }

        public String getPassword() {
            return password;
        }
    }
}
