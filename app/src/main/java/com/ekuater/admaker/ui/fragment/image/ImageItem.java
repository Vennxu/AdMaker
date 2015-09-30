package com.ekuater.admaker.ui.fragment.image;

/**
 * @author LinYong
 */
/*package*/ class ImageItem {

    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_FUNCTION = 1;

    private static final int NO_ICON = 0;

    public final int mType;
    public final int mIconId;
    public final String mThumbnailPath;
    public final String mImagePath;
    public final String mExtra;

    public ImageItem(String thumbnail, String path) {
        this(TYPE_IMAGE, NO_ICON, thumbnail, path, null);
    }

    public ImageItem(int iconId, String extra) {
        this(TYPE_FUNCTION, iconId, null, null, extra);
    }

    public ImageItem(int type, int iconId, String thumbnail, String path, String extra) {
        mType = type;
        mIconId = iconId;
        mThumbnailPath = thumbnail;
        mImagePath = path;
        mExtra = extra;
    }
}
