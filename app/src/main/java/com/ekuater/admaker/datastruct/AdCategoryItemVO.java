package com.ekuater.admaker.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/7/25.
 *
 * @author LinYong
 */
public class AdCategoryItemVO implements Parcelable {

    private int itemId;
    private int categoryId;
    private AdSticker trademark;
    private AdSticker slogan;

    public AdCategoryItemVO() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected AdCategoryItemVO(Parcel in) {
        this.itemId = in.readInt();
        this.categoryId = in.readInt();
        this.trademark = ParcelUtils.createParcelType(in, AdSticker.CREATOR);
        this.slogan = ParcelUtils.createParcelType(in, AdSticker.CREATOR);
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

    public AdSticker getTrademark() {
        return trademark;
    }

    public void setTrademark(AdSticker trademark) {
        this.trademark = trademark;
    }

    public AdSticker getSlogan() {
        return slogan;
    }

    public void setSlogan(AdSticker slogan) {
        this.slogan = slogan;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.itemId);
        dest.writeInt(this.categoryId);
        ParcelUtils.writeParcelType(dest, this.trademark, flags);
        ParcelUtils.writeParcelType(dest, this.slogan, flags);
    }

    public static final Parcelable.Creator<AdCategoryItemVO> CREATOR
            = new Parcelable.Creator<AdCategoryItemVO>() {

        public AdCategoryItemVO createFromParcel(Parcel source) {
            return new AdCategoryItemVO(source);
        }

        public AdCategoryItemVO[] newArray(int size) {
            return new AdCategoryItemVO[size];
        }
    };
}
