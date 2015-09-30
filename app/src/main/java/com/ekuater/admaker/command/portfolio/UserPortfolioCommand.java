package com.ekuater.admaker.command.portfolio;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Leo on 2015/7/2.
 *
 * @author LinYong
 */
public class UserPortfolioCommand extends BaseCommand {

    public static final String URL = "/services/portfolio/my_portfolio.json";
    public static final int COUNT_PER_PAGE = 20;

    public UserPortfolioCommand() {
        super();
        setRequestMethod(ConstantCode.REQUEST_GET);
        setUrl(URL);
    }

    public void putParamUserId(String userId) {
        putParam("userId", userId);
    }

    public void putParamQueryUserId(String queryUserId) {
        putParam("queryUserId", queryUserId);
    }

    public void putParamPage(int page) {
        putParam("page", Math.max(1, page));
    }

    @SuppressWarnings("unused")
    public static class Response extends BaseCommand.Response {

        @SerializedName("portfolioArray")
        private PortfolioVO[] portfolioVOs;

        public Response() {
            super();
        }

        public PortfolioVO[] getPortfolioVOs() {
            return portfolioVOs;
        }
    }
}
