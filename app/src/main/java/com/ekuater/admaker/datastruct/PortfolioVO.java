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
public class PortfolioVO implements Parcelable {

    private String portfolioId;
    private String userId;
    private SimpleUserVO userVO;
    private String content;
    private String adImage;
    private String adThumbImage;
    private int praiseNum;
    private int commentNum;
    private long createDate;
    @SerializedName("portfolioCommentArray")
    private PortfolioCommentVO[] commentVOs;

    public PortfolioVO() {
    }

    protected PortfolioVO(Parcel in) {
        this.portfolioId = in.readString();
        this.userId = in.readString();
        this.userVO = ParcelUtils.createParcelType(in, SimpleUserVO.CREATOR);
        this.content = in.readString();
        this.adImage = in.readString();
        this.adThumbImage = in.readString();
        this.praiseNum = in.readInt();
        this.commentNum = in.readInt();
        this.createDate = in.readLong();
        this.commentVOs = in.createTypedArray(PortfolioCommentVO.CREATOR);
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAdImage() {
        return adImage;
    }

    public void setAdImage(String adImage) {
        this.adImage = adImage;
    }

    public String getAdThumbImage() {
        return adThumbImage;
    }

    public void setAdThumbImage(String adThumbImage) {
        this.adThumbImage = adThumbImage;
    }

    public int getPraiseNum() {
        return praiseNum;
    }

    public void setPraiseNum(int praiseNum) {
        this.praiseNum = praiseNum;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public PortfolioCommentVO[] getCommentVOs() {
        return commentVOs;
    }

    public void setCommentVOs(PortfolioCommentVO[] commentVOs) {
        this.commentVOs = commentVOs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.portfolioId);
        dest.writeString(this.userId);
        ParcelUtils.writeParcelType(dest, this.userVO, flags);
        dest.writeString(this.content);
        dest.writeString(this.adImage);
        dest.writeString(this.adThumbImage);
        dest.writeInt(this.praiseNum);
        dest.writeInt(this.commentNum);
        dest.writeLong(this.createDate);
        dest.writeTypedArray(this.commentVOs, 0);
    }

    public static final Parcelable.Creator<PortfolioVO> CREATOR
            = new Parcelable.Creator<PortfolioVO>() {

        public PortfolioVO createFromParcel(Parcel source) {
            return new PortfolioVO(source);
        }

        public PortfolioVO[] newArray(int size) {
            return new PortfolioVO[size];
        }
    };
}
