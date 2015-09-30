package com.ekuater.admaker.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Leo on 2015/7/1.
 *
 * @author LinYong
 */
@SuppressWarnings("unused")
public class UserVO implements Parcelable {

    private String userId;
    @SerializedName("admakerCode")
    private String adMakerCode;
    private String email;
    private String mobile;
    @SerializedName("nickName")
    private String nickname;
    private String birthday;
    private int age;
    private String province;
    private String city;
    @SerializedName("sex")
    private int gender;
    private int constellation;
    private String school;
    private String avatar;
    private String avatarThumb;
    private String signature;

    public UserVO() {
    }

    protected UserVO(Parcel in) {
        this.userId = in.readString();
        this.adMakerCode = in.readString();
        this.email = in.readString();
        this.mobile = in.readString();
        this.nickname = in.readString();
        this.birthday = in.readString();
        this.age = in.readInt();
        this.province = in.readString();
        this.city = in.readString();
        this.gender = in.readInt();
        this.constellation = in.readInt();
        this.school = in.readString();
        this.avatar = in.readString();
        this.avatarThumb = in.readString();
        this.signature = in.readString();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getConstellation() {
        return constellation;
    }

    public void setConstellation(int constellation) {
        this.constellation = constellation;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.adMakerCode);
        dest.writeString(this.email);
        dest.writeString(this.mobile);
        dest.writeString(this.nickname);
        dest.writeString(this.birthday);
        dest.writeInt(this.age);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeInt(this.gender);
        dest.writeInt(this.constellation);
        dest.writeString(this.school);
        dest.writeString(this.avatar);
        dest.writeString(this.avatarThumb);
        dest.writeString(this.signature);
    }

    public static final Parcelable.Creator<UserVO> CREATOR
            = new Parcelable.Creator<UserVO>() {

        public UserVO createFromParcel(Parcel source) {
            return new UserVO(source);
        }

        public UserVO[] newArray(int size) {
            return new UserVO[size];
        }
    };
}
