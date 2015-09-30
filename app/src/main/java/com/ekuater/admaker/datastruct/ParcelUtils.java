package com.ekuater.admaker.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/6/19.
 *
 * @author LinYong
 */
public class ParcelUtils {

    public static <T> T createParcelType(Parcel source, Parcelable.Creator<T> c) {
        if (source.readInt() != 0) {
            return c.createFromParcel(source);
        } else {
            return null;
        }
    }

    public static <T extends Parcelable> void writeParcelType(
            Parcel dest, T val, int parcelableFlags) {
        if (val != null) {
            dest.writeInt(1);
            val.writeToParcel(dest, parcelableFlags);
        } else {
            dest.writeInt(0);
        }
    }
}
