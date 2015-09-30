package com.ekuater.admaker.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/8/10.
 *
 * @author Leo
 */
public class DayHotIssues implements Parcelable {

    private long date;
    private HotIssue[] hotIssues;

    public DayHotIssues() {
    }

    protected DayHotIssues(Parcel in) {
        this.date = in.readLong();
        this.hotIssues = (HotIssue[]) in.readParcelableArray(
                HotIssue.class.getClassLoader());
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public HotIssue[] getHotIssues() {
        return hotIssues;
    }

    public void setHotIssues(HotIssue[] hotIssues) {
        this.hotIssues = hotIssues;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.date);
        dest.writeParcelableArray(this.hotIssues, flags);
    }

    public static final Parcelable.Creator<DayHotIssues> CREATOR
            = new Parcelable.Creator<DayHotIssues>() {

        public DayHotIssues createFromParcel(Parcel source) {
            return new DayHotIssues(source);
        }

        public DayHotIssues[] newArray(int size) {
            return new DayHotIssues[size];
        }
    };
}
