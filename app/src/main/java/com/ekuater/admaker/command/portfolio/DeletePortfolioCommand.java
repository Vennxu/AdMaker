package com.ekuater.admaker.command.portfolio;

import com.ekuater.admaker.command.TokenCommand;

/**
 * Created by Leo on 2015/7/2.
 *
 * @author LinYong
 */
public class DeletePortfolioCommand extends TokenCommand {

    public static final String URL = "/services/portfolio/delete.json";

    public DeletePortfolioCommand(String token) {
        super(token);
        setUrl(URL);
    }

    public void putParamPortfolioId(String portfolioId) {
        putParam("portfolioId", portfolioId);
    }
}
