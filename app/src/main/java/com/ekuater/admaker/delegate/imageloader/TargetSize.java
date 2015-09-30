package com.ekuater.admaker.delegate.imageloader;

/**
 * Created by Leo on 2015/2/3.
 *
 * @author LinYong
 */
public class TargetSize {

    private final int width;
    private final int height;

    public TargetSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
