package com.ekuater.admaker.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/8/10.
 *
 * @author Leo
 */
@SuppressWarnings("unused")
public class HotIssue implements Parcelable {

    private int id;
    private long date;
    private int dateSerial;
    private String image;
    private String imageThumb;
    private String description;
    private long createDate;
    private long modifyDate;

    public HotIssue() {
    }

    protected HotIssue(Parcel in) {
        this.id = in.readInt();
        this.date = in.readLong();
        this.dateSerial = in.readInt();
        this.image = in.readString();
        this.imageThumb = in.readString();
        this.description = in.readString();
        this.createDate = in.readLong();
        this.modifyDate = in.readLong();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getDateSerial() {
        return dateSerial;
    }

    public void setDateSerial(int dateSerial) {
        this.dateSerial = dateSerial;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageThumb() {
        return imageThumb;
    }

    public void setImageThumb(String imageThumb) {
        this.imageThumb = imageThumb;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeLong(this.date);
        dest.writeInt(this.dateSerial);
        dest.writeString(this.image);
        dest.writeString(this.imageThumb);
        dest.writeString(this.description);
        dest.writeLong(this.createDate);
        dest.writeLong(this.modifyDate);
    }

    public static final Parcelable.Creator<HotIssue> CREATOR
            = new Parcelable.Creator<HotIssue>() {

        public HotIssue createFromParcel(Parcel source) {
            return new HotIssue(source);
        }

        public HotIssue[] newArray(int size) {
            return new HotIssue[size];
        }
    };
}
