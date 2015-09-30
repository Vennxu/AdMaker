package com.ekuater.admaker.ui.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/**
 * Created by Leo on 2015/8/13.
 *
 * @author Leo
 */
@SuppressWarnings("unused")
public final class CompatUtils {

    public static void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            view.setBackgroundDrawable(background);
        } else {
            view.setBackground(background);
        }
    }

    public static Drawable getDrawable(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(id);
        } else {
            //noinspection deprecation
            return context.getResources().getDrawable(id);
        }
    }
}
