package com.ekuater.admaker.command.adres;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.datastruct.pojo.AdTrademark;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Leo on 2015/6/17.
 *
 * @author LinYong
 */
public class AdTrademarkCommand extends BaseCommand {

    public static final String URL = "/services/source/ad_brand.json";
    public static final int COUNT_PER_PAGE = 50;

    /**
     * @param page query page number, start from 1
     */
    public AdTrademarkCommand(int page) {
        super();
        setRequestMethod(ConstantCode.REQUEST_GET);
        setUrl(URL);
        putParam("page", Math.max(1, page));
    }

    @SuppressWarnings("unused")
    public static class Response extends BaseCommand.Response {

        @SerializedName("adBrandArray")
        private AdTrademark[] trademarks;

        public Response() {
            super();
        }

        public AdTrademark[] getTrademarks() {
            return trademarks;
        }
    }
}
