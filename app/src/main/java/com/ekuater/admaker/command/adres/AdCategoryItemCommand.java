package com.ekuater.admaker.command.adres;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.datastruct.AdCategoryItem;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Leo on 2015/7/25.
 *
 * @author LinYong
 */
public class AdCategoryItemCommand extends BaseCommand {

    public static final String URL = "/services/ad_category/category_items.json";
    public static final int COUNT_PER_PAGE = 50;

    /**
     * @param categoryId query category id
     * @param page       query page number, start from 1
     */
    public AdCategoryItemCommand(int categoryId, int page) {
        super();
        setRequestMethod(ConstantCode.REQUEST_GET);
        setUrl(URL);
        putParam("categoryId", categoryId);
        putParam("page", Math.max(1, page));
    }

    @SuppressWarnings("unused")
    public static class Response extends BaseCommand.Response {

        @SerializedName("categoryItemArray")
        private AdCategoryItem[] categoryItems;

        public Response() {
            super();
        }

        public AdCategoryItem[] getCategoryItems() {
            return categoryItems;
        }
    }
}
