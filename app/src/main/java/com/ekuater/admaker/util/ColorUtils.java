package com.ekuater.admaker.util;

import android.graphics.Color;

/**
 * Created by Leo on 2015/4/8.
 *
 * @author LinYong
 */
public final class ColorUtils {

    public static int parseColor(String colorString) {
        return Color.parseColor(colorString);
    }

    public static String toColorString(int color) {
        return "#" + Integer.toHexString(color).toUpperCase();
    }
}
