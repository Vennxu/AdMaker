package com.ekuater.admaker.command.portfolio;

import com.ekuater.admaker.command.BaseCommand;

/**
 * Created by Leo on 2015/7/2.
 *
 * @author LinYong
 */
public class PraisePortfolioCommand extends BaseCommand {

    public static final String URL = "/services/portfolio/praise.json";

    public PraisePortfolioCommand() {
        super();
        setUrl(URL);
    }

    public void putParamPortfolioId(String portfolioId) {
        putParam("portfolioId", portfolioId);
    }
}
