package com.ekuater.admaker.command.portfolio;

import com.ekuater.admaker.command.TokenCommand;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.datastruct.PortfolioVO;

/**
 * Created by Leo on 2015/7/2.
 *
 * @author LinYong
 */
public class PortfolioCommentsCommand extends TokenCommand {

    public static final String URL = "/services/portfolio/comment_list.json";
    public static final int COUNT_PER_PAGE = 20;

    public PortfolioCommentsCommand(String token) {
        super(token);
        setRequestMethod(ConstantCode.REQUEST_GET);
        setUrl(URL);
    }

    public void putParamPortfolioId(String portfolioId) {
        putParam("portfolioId", portfolioId);
    }

    public void putParamPage(int page) {
        putParam("page", Math.max(1, page));
    }

    @SuppressWarnings("unused")
    public static class Response extends TokenCommand.Response {

        private PortfolioVO portfolioVO;

        public Response() {
            super();
        }

        public PortfolioVO getPortfolioVO() {
            return portfolioVO;
        }
    }
}
