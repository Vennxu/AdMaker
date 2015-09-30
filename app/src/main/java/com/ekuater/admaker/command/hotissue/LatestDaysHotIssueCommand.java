package com.ekuater.admaker.command.hotissue;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.datastruct.HotIssue;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Leo on 2015/8/10.
 * 获取最近几天热门事件资源
 *
 * @author Leo
 */
public class LatestDaysHotIssueCommand extends BaseCommand {

    public static final String URL = "/services/ad_hot_issue/latest_days_issues.json";
    public static final int COUNT_PER_PAGE = 50;

    public LatestDaysHotIssueCommand() {
        super();
        setRequestMethod(ConstantCode.REQUEST_GET);
        setUrl(URL);
    }

    /**
     * 最近几天的热门事件资源
     *
     * @param days 最近天数
     */
    public void putParamDays(int days) {
        putParam("days", days);
    }

    /**
     * 每天几条热门事件资源
     *
     * @param topCount 条数
     */
    public void putParamTopCount(int topCount) {
        putParam("topCount", topCount);
    }

    /**
     * 分页数
     *
     * @param page query page number, start from 1
     */
    public void putParamPage(int page) {
        putParam("page", Math.max(1, page));
    }

    @SuppressWarnings("unused")
    public static class Response extends BaseCommand.Response {

        @SerializedName("hotIssueArray")
        private HotIssue[] hotIssues;

        public Response() {
            super();
        }

        public HotIssue[] getHotIssues() {
            return hotIssues;
        }
    }
}
