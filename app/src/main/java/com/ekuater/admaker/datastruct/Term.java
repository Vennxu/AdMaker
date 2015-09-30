package com.ekuater.admaker.datastruct;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.ekuater.admaker.util.ColorUtils;

/**
 * Created by Administrator on 2015/6/9.
 *
 * @author XuWenxiang
 */
@SuppressWarnings("unused")
public class Term implements Parcelable {

    private static final int DEFAULT_TAG_COLOR = Color.WHITE;

    public final static int DEFAULT = -1;
    public final static int FONT = 0;
    public final static int COLOR = 1;
    public final static int EFFECT = 2;

    private String font;
    private String color;

    public Term() {
    }

    public Term(String color, String flag) {
        this.color = color;
    }

    public Term(String content) {
        this.font = content;
    }

    public Term(Parcel parcel) {
        this.font = parcel.readString();
        this.color = parcel.readString();
    }

    public int parseTagColor() {
        return parseColor(this.color, DEFAULT_TAG_COLOR);
    }

    private int parseColor(String colorString, int defaultColor) {
        if (!TextUtils.isEmpty(colorString)) {
            try {
                return ColorUtils.parseColor(colorString);
            } catch (Exception e) {
                return defaultColor;
            }
        } else {
            return defaultColor;
        }
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(font);
        dest.writeString(color);
    }

    public static final Creator<Term> CREATOR = new Creator<Term>() {
        @Override
        public Term createFromParcel(Parcel source) {
            return new Term(source);
        }

        @Override
        public Term[] newArray(int size) {
            return new Term[size];
        }
    };
}
