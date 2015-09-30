package com.ekuater.admaker.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/7/25.
 *
 * @author LinYong
 */
@SuppressWarnings("unused")
public class AdCategoryVO implements Parcelable {

    private int categoryId;
    private int serialNum;
    private String categoryName;

    public AdCategoryVO() {
    }

    protected AdCategoryVO(Parcel in) {
        this.categoryId = in.readInt();
        this.serialNum = in.readInt();
        this.categoryName = in.readString();
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(int serialNum) {
        this.serialNum = serialNum;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.categoryId);
        dest.writeInt(this.serialNum);
        dest.writeString(this.categoryName);
    }

    public static final Parcelable.Creator<AdCategoryVO> CREATOR
            = new Parcelable.Creator<AdCategoryVO>() {

        public AdCategoryVO createFromParcel(Parcel source) {
            return new AdCategoryVO(source);
        }

        public AdCategoryVO[] newArray(int size) {
            return new AdCategoryVO[size];
        }
    };
}
