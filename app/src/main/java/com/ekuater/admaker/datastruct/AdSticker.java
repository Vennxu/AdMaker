package com.ekuater.admaker.datastruct;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by Leo on 2015/6/1.
 *
 * @author LinYong
 */
@SuppressWarnings("unused")
public class AdSticker implements Parcelable {

    public enum Type {
        SLOGAN,          // 广告语
        TRADEMARK,       // 商标
    }

    public enum From {
        INTERNAL,
        LOCAL,
        ONLINE,
    }

    private String id;
    private String title;
    private String thumb;
    private String image;
    private String altImage; // alternative image, may be vertical slogan
    private From from; // where AdSticker from
    private Type type;

    public AdSticker() {
    }

    public AdSticker(String id, From from, Type type, String title,
                     String thumb, String image, String altImage) {
        this.id = id;
        this.title = title;
        this.thumb = thumb;
        this.image = image;
        this.altImage = altImage;
        this.from = from;
        this.type = type;
    }

    protected AdSticker(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.thumb = in.readString();
        this.image = in.readString();
        this.altImage = in.readString();
        int tmpSource = in.readInt();
        this.from = tmpSource == -1 ? null : From.values()[tmpSource];
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : Type.values()[tmpType];
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAltImage() {
        return altImage;
    }

    public void setAltImage(String altImage) {
        this.altImage = altImage;
    }

    public From getFrom() {
        return from;
    }

    public void setFrom(From from) {
        this.from = from;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isLegal() {
        return !TextUtils.isEmpty(id) && !TextUtils.isEmpty(image);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.thumb);
        dest.writeString(this.image);
        dest.writeString(this.altImage);
        dest.writeInt(this.from == null ? -1 : this.from.ordinal());
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
    }

    public static final Creator<AdSticker> CREATOR = new Creator<AdSticker>() {
        public AdSticker createFromParcel(Parcel source) {
            return new AdSticker(source);
        }

        public AdSticker[] newArray(int size) {
            return new AdSticker[size];
        }
    };
}
