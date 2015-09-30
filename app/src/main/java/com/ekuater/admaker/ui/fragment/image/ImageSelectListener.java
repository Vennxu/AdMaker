package com.ekuater.admaker.ui.fragment.image;

/**
 * @author LinYong
 */
public interface ImageSelectListener {

    public void onSelectSuccess(String imagePath, boolean isTemp);

    public void onMultiSelectSuccess(String [] imagePaths);

    public void onSelectFailure();
}
