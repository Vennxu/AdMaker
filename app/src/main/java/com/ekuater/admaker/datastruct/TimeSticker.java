package com.ekuater.admaker.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/6/27.
 *
 * @author FanChong
 */
@SuppressWarnings("unused")
public class TimeSticker extends AdSticker {

    private long time;

    public TimeSticker() {
        super();
    }

    public TimeSticker(AdSticker sticker, long time) {
        super(sticker.getId(), sticker.getFrom(), sticker.getType(), sticker.getTitle(),
                sticker.getThumb(), sticker.getImage(), sticker.getAltImage());
        setTime(time);
    }

    protected TimeSticker(Parcel in) {
        super(in);
        this.time = in.readLong();
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) || (o instanceof TimeSticker && idEquals((TimeSticker) o));
    }

    private boolean idEquals(TimeSticker other) {
        return (getId() == null && other.getId() == null)
                || (getId() != null && getId().equals(other.getId()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.time);
    }

    public static final Parcelable.Creator<TimeSticker> CREATOR
            = new Parcelable.Creator<TimeSticker>() {

        public TimeSticker createFromParcel(Parcel source) {
            return new TimeSticker(source);
        }

        public TimeSticker[] newArray(int size) {
            return new TimeSticker[size];
        }
    };
}
