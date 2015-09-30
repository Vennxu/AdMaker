package com.ekuater.admaker.datastruct.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/6/17.
 *
 * @author LinYong
 */
@SuppressWarnings("unused")
public class AdSlogan implements Parcelable {

    private int id;
    private String imgName;
    private String horzWordImg;
    private String vertWordImg;
    private int serialNum;

    public AdSlogan() {
    }

    protected AdSlogan(Parcel in) {
        this.id = in.readInt();
        this.imgName = in.readString();
        this.horzWordImg = in.readString();
        this.vertWordImg = in.readString();
        this.serialNum = in.readInt();
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

    public String getHorzWordImg() {
        return horzWordImg;
    }

    public void setHorzWordImg(String horzWordImg) {
        this.horzWordImg = horzWordImg;
    }

    public String getVertWordImg() {
        return vertWordImg;
    }

    public void setVertWordImg(String vertWordImg) {
        this.vertWordImg = vertWordImg;
    }

    public int getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(int serialNum) {
        this.serialNum = serialNum;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.imgName);
        dest.writeString(this.horzWordImg);
        dest.writeString(this.vertWordImg);
        dest.writeInt(this.serialNum);
    }

    public static final Parcelable.Creator<AdSlogan> CREATOR
            = new Parcelable.Creator<AdSlogan>() {

        public AdSlogan createFromParcel(Parcel source) {
            return new AdSlogan(source);
        }

        public AdSlogan[] newArray(int size) {
            return new AdSlogan[size];
        }
    };
}
