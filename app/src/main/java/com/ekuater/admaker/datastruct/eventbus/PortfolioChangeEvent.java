package com.ekuater.admaker.datastruct.eventbus;

import com.ekuater.admaker.datastruct.PortfolioVO;

/**
 * Created by Administrator on 2015/7/8.
 */
public class PortfolioChangeEvent {

    private int position;
    private PortfolioVO portfolio;
    private String tag;

    public PortfolioChangeEvent(int position, PortfolioVO portfolio, String tag){
        this.position = position;
        this.portfolio = portfolio;
        this.tag = tag;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public PortfolioVO getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(PortfolioVO portfolio) {
        this.portfolio = portfolio;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
