package com.ekuater.admaker.datastruct.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/6/17.
 *
 * @author LinYong
 */
@SuppressWarnings("unused")
public class ResVersion implements Parcelable {

    // Type
    public static final String AD_SOURCE = "AD_SOURCE";

    // Source
    public static final String AD_SCENE = "AD_SCENE";
    public static final String AD_SLOGAN = "AD_WORD";
    public static final String AD_TRADEMARK = "AD_BRAND";
    public static final String AD_CATEGORY = "AD_CATEGORY";

    private int id;
    private String type;
    private String source;
    private int version;

    public ResVersion() {
    }

    protected ResVersion(Parcel in) {
        this.id = in.readInt();
        this.type = in.readString();
        this.source = in.readString();
        this.version = in.readInt();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.type);
        dest.writeString(this.source);
        dest.writeInt(this.version);
    }

    public static final Parcelable.Creator<ResVersion> CREATOR
            = new Parcelable.Creator<ResVersion>() {

        public ResVersion createFromParcel(Parcel source) {
            return new ResVersion(source);
        }

        public ResVersion[] newArray(int size) {
            return new ResVersion[size];
        }
    };
}
