package com.ekuater.admaker.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

import com.ekuater.admaker.datastruct.pojo.AdSlogan;
import com.ekuater.admaker.datastruct.pojo.AdTrademark;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Leo on 2015/7/25.
 *
 * @author LinYong
 */
@SuppressWarnings("unused")
public class AdCategoryItem implements Parcelable {

    @SerializedName("id")
    private int itemId;

    private int categoryId;

    @SerializedName("adBrand")
    private AdTrademark trademark;

    @SerializedName("adWord")
    private AdSlogan slogan;

    public AdCategoryItem() {
    }

    protected AdCategoryItem(Parcel in) {
        this.itemId = in.readInt();
        this.categoryId = in.readInt();
        this.trademark = in.readParcelable(AdTrademark.class.getClassLoader());
        this.slogan = in.readParcelable(AdSlogan.class.getClassLoader());
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public AdTrademark getTrademark() {
        return trademark;
    }

    public void setTrademark(AdTrademark trademark) {
        this.trademark = trademark;
    }

    public AdSlogan getSlogan() {
        return slogan;
    }

    public void setSlogan(AdSlogan slogan) {
        this.slogan = slogan;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.itemId);
        dest.writeInt(this.categoryId);
        dest.writeParcelable(this.trademark, 0);
        dest.writeParcelable(this.slogan, 0);
    }

    public static final Parcelable.Creator<AdCategoryItem> CREATOR
            = new Parcelable.Creator<AdCategoryItem>() {

        public AdCategoryItem createFromParcel(Parcel source) {
            return new AdCategoryItem(source);
        }

        public AdCategoryItem[] newArray(int size) {
            return new AdCategoryItem[size];
        }
    };
}
