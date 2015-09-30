package com.ekuater.admaker.command.portfolio;

import com.ekuater.admaker.command.TokenCommand;
import com.ekuater.admaker.datastruct.PortfolioCommentVO;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Leo on 2015/7/2.
 *
 * @author LinYong
 */
public class CommentPortfolioCommand extends TokenCommand {

    public static final String URL = "/services/portfolio/comment.json";

    public CommentPortfolioCommand(String token) {
        super(token);
        setUrl(URL);
    }

    public void putParamPortfolioId(String portfolioId) {
        putParam("portfolioId", portfolioId);
    }

    public void putParamComment(String comment) {
        putParam("comment", comment);
    }

    public void putParamParentCommentId(String parentCommentId) {
        putParam("parentCommentId", parentCommentId);
    }

    public void putParamReplyComment(String replyComment) {
        putParam("replyComment", replyComment);
    }

    public void putParamReplyNickname(String replyNickname) {
        putParam("replyNickName", replyNickname);
    }

    public void putParamReplyUserId(String replyUserId) {
        putParam("replyUserId", replyUserId);
    }

    @SuppressWarnings("unused")
    public static class Response extends TokenCommand.Response {

        @SerializedName("portfolioCommentVO")
        private PortfolioCommentVO commentVO;

        public Response() {
            super();
        }

        public PortfolioCommentVO getCommentVO() {
            return commentVO;
        }
    }
}
