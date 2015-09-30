package com.ekuater.admaker.command.adres;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.datastruct.AdCategoryVO;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Leo on 2015/7/25.
 *
 * @author LinYong
 */
public class AdCategoryCommand extends BaseCommand {

    public static final String URL = "/services/ad_category/all_category.json";
    public static final int COUNT_PER_PAGE = 50;

    /**
     * @param page query page number, start from 1
     */
    public AdCategoryCommand(int page) {
        super();
        setRequestMethod(ConstantCode.REQUEST_GET);
        setUrl(URL);
        putParam("page", Math.max(1, page));
    }

    @SuppressWarnings("unused")
    public static class Response extends BaseCommand.Response {

        @SerializedName("categoryArray")
        private AdCategoryVO[] categories;

        public Response() {
            super();
        }

        public AdCategoryVO[] getCategories() {
            return categories;
        }
    }
}
