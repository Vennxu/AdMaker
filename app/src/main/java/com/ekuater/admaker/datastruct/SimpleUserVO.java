package com.ekuater.admaker.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Leo on 2015/7/2.
 *
 * @author LinYong
 */
@SuppressWarnings("unused")
public class SimpleUserVO implements Parcelable {

    private String userId;
    @SerializedName("admakerCode")
    private String adMakerCode;
    @SerializedName("nickName")
    private String nickname;
    @SerializedName("sex")
    private int gender;
    private String avatar;
    private String avatarThumb;

    public SimpleUserVO(){
    }

    protected SimpleUserVO(Parcel in) {
        this.userId = in.readString();
        this.adMakerCode = in.readString();
        this.nickname = in.readString();
        this.gender = in.readInt();
        this.avatar = in.readString();
        this.avatarThumb = in.readString();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAdMakerCode() {
        return adMakerCode;
    }

    public void setAdMakerCode(String adMakerCode) {
        this.adMakerCode = adMakerCode;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatarThumb() {
        return avatarThumb;
    }

    public void setAvatarThumb(String avatarThumb) {
        this.avatarThumb = avatarThumb;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.adMakerCode);
        dest.writeString(this.nickname);
        dest.writeInt(this.gender);
        dest.writeString(this.avatar);
        dest.writeString(this.avatarThumb);
    }

    public static final Parcelable.Creator<SimpleUserVO> CREATOR
            = new Parcelable.Creator<SimpleUserVO>() {

        public SimpleUserVO createFromParcel(Parcel source) {
            return new SimpleUserVO(source);
        }

        public SimpleUserVO[] newArray(int size) {
            return new SimpleUserVO[size];
        }
    };
}
