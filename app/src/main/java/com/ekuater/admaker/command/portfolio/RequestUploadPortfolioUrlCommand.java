package com.ekuater.admaker.command.portfolio;

import com.ekuater.admaker.command.TokenCommand;

/**
 * Created by Leo on 2015/7/2.
 *
 * @author LinYong
 */
public class RequestUploadPortfolioUrlCommand extends TokenCommand {

    public static final String URL = "/services/portfolio/upload_portfolio_url.json";

    public RequestUploadPortfolioUrlCommand(String token) {
        super(token);
        setUrl(URL);
        putParam("token", token);
    }

    public void putParamContent(String content) {
        putParam("content", content);
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
