package com.ekuater.admaker.datastruct;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/6/1
 *
 * @author Xu wenxiang
 */
@SuppressWarnings("unused")
public class Scene implements Parcelable {

    private String image;
    private String imageThumb;
    private Point leftTop;
    private Point rightTop;
    private Point leftBottom;
    private Point rightBottom;
    private Point contentSize;

    public Scene() {
    }

    protected Scene(Parcel in) {
        this.image = in.readString();
        this.imageThumb = in.readString();
        this.leftTop = ParcelUtils.createParcelType(in, Point.CREATOR);
        this.leftBottom = ParcelUtils.createParcelType(in, Point.CREATOR);
        this.rightTop = ParcelUtils.createParcelType(in, Point.CREATOR);
        this.rightBottom = ParcelUtils.createParcelType(in, Point.CREATOR);
        this.contentSize = ParcelUtils.createParcelType(in, Point.CREATOR);
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

    public Point getLeftTop() {
        return leftTop;
    }

    public void setLeftTop(Point leftTop) {
        this.leftTop = leftTop;
    }

    public Point getRightTop() {
        return rightTop;
    }

    public void setRightTop(Point rightTop) {
        this.rightTop = rightTop;
    }

    public Point getLeftBottom() {
        return leftBottom;
    }

    public void setLeftBottom(Point leftBottom) {
        this.leftBottom = leftBottom;
    }

    public Point getRightBottom() {
        return rightBottom;
    }

    public void setRightBottom(Point rightBottom) {
        this.rightBottom = rightBottom;
    }

    public Point getContentSize() {
        return contentSize;
    }

    public void setContentSize(Point contentSize) {
        this.contentSize = contentSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(image);
        dest.writeString(imageThumb);
        ParcelUtils.writeParcelType(dest, this.leftTop, flags);
        ParcelUtils.writeParcelType(dest, this.leftBottom, flags);
        ParcelUtils.writeParcelType(dest, this.rightTop, flags);
        ParcelUtils.writeParcelType(dest, this.rightBottom, flags);
        ParcelUtils.writeParcelType(dest, this.contentSize, flags);
    }

    public static final Creator<Scene> CREATOR = new Creator<Scene>() {

        @Override
        public Scene createFromParcel(Parcel source) {
            return new Scene(source);
        }

        @Override
        public Scene[] newArray(int size) {
            return new Scene[size];
        }
    };
}
