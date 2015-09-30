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
public class LatestPortfolioCommand extends BaseCommand {

    public static final String URL = "/services/portfolio/latest_portfolio.json";
    public static final int COUNT_PER_PAGE = 20;

    public LatestPortfolioCommand(int page) {
        super();
        setRequestMethod(ConstantCode.REQUEST_GET);
        setUrl(URL);
        putParam("page", Math.max(1, page));
    }

    public void putParamPageSize(int pageSize) {
        putParam("pageSize", pageSize);
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
