package com.ekuater.admaker.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Leo on 2015/1/7.
 *
 * @author LinYong
 */
public final class BmpUtils {

    private static final String TAG = BmpUtils.class.getSimpleName();

    public static Bitmap zoomDownBitmap(File bmpFile, int maxWidth, int maxHeight) {
        return zoomDownBitmap(bmpFile, maxWidth, maxHeight, Bitmap.Config.RGB_565);
    }

    public static Bitmap zoomDownBitmap(File bmpFile, int maxWidth, int maxHeight,
                                        Bitmap.Config config) {
        if (bmpFile == null || !bmpFile.isFile()) {
            throw new IllegalArgumentException("zoomDownBitmap: illegal bitmap file");
        }

        if (maxHeight <= 0 || maxHeight <= 0) {
            throw new IllegalArgumentException("zoomDownBitmap: illegal max size");
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        String bmpFilePath = bmpFile.getPath();
        Bitmap bitmap;

        // get image real size
        options.inPreferredConfig = config;
        options.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeFile(bmpFilePath, options);
        float realWidth = options.outWidth;
        float realHeight = options.outHeight;

        // decode the thumbnail image now
        if (realHeight > 0 && realWidth > 0) {
            int scale = (int) (Math.max(realWidth / maxWidth,
                    realHeight / maxHeight) + 0.5F);
            options.inSampleSize = (scale >= 1) ? scale : 1;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(bmpFilePath, options);
        }

        return bitmap;
    }

    public static Bitmap zoomDownBitmap(Bitmap oriBmp, int maxWidth, int maxHeight) {
        if (oriBmp == null) {
            throw new IllegalArgumentException("zoomDownBitmap: empty bitmap");
        }

        if (maxHeight <= 0 || maxHeight <= 0) {
            throw new IllegalArgumentException("zoomDownBitmap: illegal max size");
        }

        Bitmap bitmap = null;
        float realWidth = oriBmp.getWidth();
        float realHeight = oriBmp.getHeight();

        // decode the thumbnail image now
        if (realHeight > 0 && realWidth > 0) {
            float scale = Math.min(maxWidth / realWidth, maxHeight / realHeight);
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            bitmap = Bitmap.createBitmap(oriBmp, 0, 0, (int) realWidth,
                    (int) realHeight, matrix, true);
        }

        return bitmap;
    }

    public static Bitmap compressBitmapByQuality(Bitmap bitmap, int quality) {
        quality = Math.min(100, Math.max(10, quality));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                outputStream.toByteArray());
        return BitmapFactory.decodeStream(inputStream, null, null);
    }

    public static Bitmap compressBitmapBySize(Bitmap bitmap, int limitSize) {
        if (limitSize <= 0) {
            throw new IllegalArgumentException("compressBitmapBySize: illegal limit size");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int quality = 100;

        do {
            outputStream.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            quality -= 10;
        } while (outputStream.toByteArray().length / 1024 > limitSize && quality > 0);

        L.v(TAG, "compressBitmapBySize(), size=%1$d KB", outputStream.toByteArray().length / 1024);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                outputStream.toByteArray());
        return BitmapFactory.decodeStream(inputStream, null, null);
    }

    public static Bitmap saveBitmapToFile(Bitmap bitmap, File outFile) {
        return saveBitmap(bitmap, outFile, Bitmap.CompressFormat.JPEG, 100);
    }

    public static Bitmap saveBitmapJpg(Bitmap bitmap, File outFile) {
        return saveBitmap(bitmap, outFile, Bitmap.CompressFormat.JPEG, 100);
    }

    public static Bitmap saveBitmapPng(Bitmap bitmap, File outFile) {
        return saveBitmap(bitmap, outFile, Bitmap.CompressFormat.PNG, 100);
    }

    public static Bitmap saveBitmap(Bitmap bitmap, File outFile,
                                    Bitmap.CompressFormat format, int quality) {
        File parentDir = outFile.getParentFile();
        FileOutputStream out = null;

        try {
            if (parentDir.exists() || parentDir.mkdirs()) {
                out = new FileOutputStream(outFile);
                bitmap.compress(format, quality, out);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bitmap;
    }

    public static Bitmap setScaleImage(Activity activity, Bitmap bm) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        int srcWidth = display.getWidth() - dp2px(activity, 80);
        float scaleWidth = (float) srcWidth / bm.getWidth();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleWidth);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                bm.getHeight(), matrix, true);
    }

    public static int dp2px(Context context, float dp) {
        return dp2px(context.getResources(), dp);
    }

    public static int dp2px(Resources res, float dp) {
        return dp2px(res.getDisplayMetrics(), dp);
    }

    public static int dp2px(DisplayMetrics metrics, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                metrics);
    }

}
