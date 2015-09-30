package com.ekuater.admaker.datastruct.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/6/17.
 *
 * @author LinYong
 */
@SuppressWarnings("unused")
public class AdTrademark implements Parcelable {

    private int id;
    private String imgName;
    private String brandImg;
    private int serialNum;

    public AdTrademark() {
    }

    protected AdTrademark(Parcel in) {
        this.id = in.readInt();
        this.imgName = in.readString();
        this.brandImg = in.readString();
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

    public String getBrandImg() {
        return brandImg;
    }

    public void setBrandImg(String brandImg) {
        this.brandImg = brandImg;
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
        dest.writeString(this.brandImg);
        dest.writeInt(this.serialNum);
    }

    public static final Parcelable.Creator<AdTrademark> CREATOR
            = new Parcelable.Creator<AdTrademark>() {

        public AdTrademark createFromParcel(Parcel source) {
            return new AdTrademark(source);
        }

        public AdTrademark[] newArray(int size) {
            return new AdTrademark[size];
        }
    };
}
