package com.ekuater.admaker.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Leo on 2015/7/2.
 *
 * @author LinYong
 */
@SuppressWarnings("unused")
public class PortfolioCommentVO implements Parcelable {

    @SerializedName("portfolioCommentId")
    private String commentId;
    private String portfolioId;
    private String userId;
    private SimpleUserVO userVO;
    private String comment;
    private String parentCommentId;
    private String replyComment;
    @SerializedName("replyNickName")
    private String replyNickname;
    private String replyUserId;
    private long createDate;

    public PortfolioCommentVO() {
    }

    protected PortfolioCommentVO(Parcel in) {
        this.commentId = in.readString();
        this.portfolioId = in.readString();
        this.userId = in.readString();
        this.userVO = ParcelUtils.createParcelType(in, SimpleUserVO.CREATOR);
        this.comment = in.readString();
        this.parentCommentId = in.readString();
        this.replyComment = in.readString();
        this.replyNickname = in.readString();
        this.replyUserId = in.readString();
        this.createDate = in.readLong();
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public SimpleUserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(SimpleUserVO userVO) {
        this.userVO = userVO;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getReplyComment() {
        return replyComment;
    }

    public void setReplyComment(String replyComment) {
        this.replyComment = replyComment;
    }

    public String getReplyNickname() {
        return replyNickname;
    }

    public void setReplyNickname(String replyNickname) {
        this.replyNickname = replyNickname;
    }

    public String getReplyUserId() {
        return replyUserId;
    }

    public void setReplyUserId(String replyUserId) {
        this.replyUserId = replyUserId;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.commentId);
        dest.writeString(this.portfolioId);
        dest.writeString(this.userId);
        ParcelUtils.writeParcelType(dest, this.userVO, flags);
        dest.writeString(this.comment);
        dest.writeString(this.parentCommentId);
        dest.writeString(this.replyComment);
        dest.writeString(this.replyNickname);
        dest.writeString(this.replyUserId);
        dest.writeLong(this.createDate);
    }

    public static final Parcelable.Creator<PortfolioCommentVO> CREATOR
            = new Parcelable.Creator<PortfolioCommentVO>() {

        public PortfolioCommentVO createFromParcel(Parcel source) {
            return new PortfolioCommentVO(source);
        }

        public PortfolioCommentVO[] newArray(int size) {
            return new PortfolioCommentVO[size];
        }
    };
}
