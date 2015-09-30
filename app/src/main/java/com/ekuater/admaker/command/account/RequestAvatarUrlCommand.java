package com.ekuater.admaker.command.account;

import com.ekuater.admaker.command.TokenCommand;

/**
 * Created by Leo on 2015/6/29.
 * request new avatar url to upload avatar image to qiniu server
 *
 * @author LinYong
 */
public class RequestAvatarUrlCommand extends TokenCommand {

    public static final String URL = "/services/user/upload_avatar_url.json";

    public RequestAvatarUrlCommand(String token) {
        super(token);
        setUrl(URL);
        putParam("token", token);
    }

    public void putParamExtName(String extName) {
        putParam("extName", extName);
    }

    @SuppressWarnings("unused")
    public static class Response extends TokenCommand.Response {

        private String qiNiuToken;
        private String qiNiuKey;

        public Response() {
            super();
        }

        public String getQiNiuToken() {
            return qiNiuToken;
        }

        public String getQiNiuKey() {
            return qiNiuKey;
        }
    }
}
