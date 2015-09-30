package com.ekuater.admaker.ui.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.TypedValue;

/**
 * Created by Leo on 2015/8/13.
 *
 * @author Leo
 */
@SuppressWarnings("unused")
public final class ViewUtils {

    public static int dpToPx(final float dp, Context context) {
        return dpToPx(dp, context.getResources());
    }

    public static int dpToPx(final float dp, Resources resources) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, resources.getDisplayMetrics()));
    }

    public static void rotateRect(@NonNull RectF rect, @NonNull PointF center,
                                  float rotateDegree) {
        PointF rectCenter = new PointF(rect.centerX(), rect.centerY());
        rotatePoint(rectCenter, center, rotateDegree);
        rect.offset(rectCenter.x - rect.centerX(), rectCenter.y - rect.centerY());
    }

    public static void rotatePoint(@NonNull PointF point, @NonNull PointF center,
                                   float rotateDegree) {
        final float sinA = (float) Math.sin(Math.toRadians(rotateDegree));
        final float cosA = (float) Math.cos(Math.toRadians(rotateDegree));
        final float newX = center.x + (point.x - center.x) * cosA - (point.y - center.y) * sinA;
        final float newY = center.y + (point.y - center.y) * cosA + (point.x - center.x) * sinA;
        point.set(newX, newY);
    }
}
