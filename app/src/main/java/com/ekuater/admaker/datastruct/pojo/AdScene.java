package com.ekuater.admaker.datastruct.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/6/17.
 *
 * @author LinYong
 */
@SuppressWarnings("unused")
public class AdScene implements Parcelable {

    private int id;
    private String imgName;
    private String sceneImg;
    private String sceneThumbImg;
    private int serialNum;
    private String leftTop;
    private String rightTop;
    private String leftBottom;
    private String rightBottom;
    private String contentSize;

    public AdScene() {
    }

    protected AdScene(Parcel in) {
        this.id = in.readInt();
        this.imgName = in.readString();
        this.sceneImg = in.readString();
        this.sceneThumbImg = in.readString();
        this.serialNum = in.readInt();
        this.leftTop = in.readString();
        this.rightTop = in.readString();
        this.leftBottom = in.readString();
        this.rightBottom = in.readString();
        this.contentSize = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getSceneImg() {
        return sceneImg;
    }

    public void setSceneImg(String sceneImg) {
        this.sceneImg = sceneImg;
    }

    public String getSceneThumbImg() {
        return sceneThumbImg;
    }

    public void setSceneThumbImg(String sceneThumbImg) {
        this.sceneThumbImg = sceneThumbImg;
    }

    public int getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(int serialNum) {
        this.serialNum = serialNum;
    }

    public String getLeftTop() {
        return leftTop;
    }

    public void setLeftTop(String leftTop) {
        this.leftTop = leftTop;
    }

    public String getRightTop() {
        return rightTop;
    }

    public void setRightTop(String rightTop) {
        this.rightTop = rightTop;
    }

    public String getLeftBottom() {
        return leftBottom;
    }

    public void setLeftBottom(String leftBottom) {
        this.leftBottom = leftBottom;
    }

    public String getRightBottom() {
        return rightBottom;
    }

    public void setRightBottom(String rightBottom) {
        this.rightBottom = rightBottom;
    }

    public String getContentSize() {
        return contentSize;
    }

    public void setContentSize(String contentSize) {
        this.contentSize = contentSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.imgName);
        dest.writeString(this.sceneImg);
        dest.writeString(this.sceneThumbImg);
        dest.writeInt(this.serialNum);
        dest.writeString(this.leftTop);
        dest.writeString(this.rightTop);
        dest.writeString(this.leftBottom);
        dest.writeString(this.rightBottom);
        dest.writeString(this.contentSize);
    }

    public static final Creator<AdScene> CREATOR = new Creator<AdScene>() {

        public AdScene createFromParcel(Parcel source) {
            return new AdScene(source);
        }

        public AdScene[] newArray(int size) {
            return new AdScene[size];
        }
    };
}
