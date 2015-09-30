package com.ekuater.admaker.ui.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;

import com.ekuater.admaker.datastruct.Scene;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Leo on 2015/6/1.
 *
 * @author LinYong
 */
public class BitmapUtils {

    /**
     * Used to tag logs
     */
    @SuppressWarnings("unused")
    private static final String TAG = "BitmapUtils";

    public static final long MAX_SIZE = 1024 * 512;// 500KB

    public static Bitmap loadImageByPath(final String imagePath, int reqWidth,
                                         int reqHeight) {
        File file = new File(imagePath);
        if (file.length() < MAX_SIZE) {
            return getSampledBitmap(imagePath, reqWidth, reqHeight);
        } else {// 压缩图片
            return getImageCompress(imagePath);
        }
    }

    public static int getOrientation(final String imagePath) {
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static Bitmap getSampledBitmap(String filePath, int reqWidth,
                                          int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = (int) FloatMath
                        .floor(((float) height / reqHeight) + 0.5f); // Math.round((float)height
                // /
                // (float)reqHeight);
            } else {
                inSampleSize = (int) FloatMath
                        .floor(((float) width / reqWidth) + 0.5f); // Math.round((float)width
                // /
                // (float)reqWidth);
            }
        }
        // System.out.println("inSampleSize--->"+inSampleSize);

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    public static BitmapSize getBitmapSize(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        return new BitmapSize(options.outWidth, options.outHeight);
    }

    public static BitmapSize getScaledSize(int originalWidth,
                                           int originalHeight, int numPixels) {
        float ratio = (float) originalWidth / originalHeight;

        int scaledHeight = (int) FloatMath.sqrt((float) numPixels / ratio);
        int scaledWidth = (int) (ratio * FloatMath.sqrt((float) numPixels
                / ratio));

        return new BitmapSize(scaledWidth, scaledHeight);
    }

    public static class BitmapSize {
        public int width;
        public int height;

        public BitmapSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public static byte[] bitmapTobytes(Bitmap bitmap) {
        ByteArrayOutputStream a = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 30, a);
        return a.toByteArray();
    }

    public static byte[] bitmapTobytesNoCompress(Bitmap bitmap) {
        ByteArrayOutputStream a = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, a);
        return a.toByteArray();
    }

    public static Bitmap genRotateBitmap(byte[] data) {
        Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length);
        // 自定义相机拍照需要旋转90预览支持竖屏
        Matrix matrix = new Matrix();// 矩阵
        matrix.reset();// 设置为单位矩阵
        matrix.postRotate(90);// 旋转90度
        Bitmap bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(),
                bMap.getHeight(), matrix, true);
        bMap.recycle();
        bMap = null;
        System.gc();
        return bMapRotate;
    }

    public static Bitmap byteToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    /**
     * 将view转为bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    // 按大小缩放
    public static Bitmap getImageCompress(final String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    // 图片按比例大小压缩
    public static Bitmap compress(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    // 图片质量压缩
    private static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;

        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
//			System.out.println("options--->" + options + "    "
//					+ (baos.toByteArray().length / 1024));
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public void printscreen_share(View v, Activity context) {
        View view1 = context.getWindow().getDecorView();
        Display display = context.getWindowManager().getDefaultDisplay();
        view1.layout(0, 0, display.getWidth(), display.getHeight());
        view1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view1.getDrawingCache());
    }

    // 图片转为文件
    public static boolean saveBitmap2file(Bitmap bmp, String filepath) {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
        int quality = 100;
        OutputStream stream = null;
        try {
            // 判断SDcard状态
            if (!Environment.MEDIA_MOUNTED.equals(Environment
                    .getExternalStorageState())) {
                // 错误提示
                return false;
            }

            // 检查SDcard空间
            File SDCardRoot = Environment.getExternalStorageDirectory();
            if (SDCardRoot.getFreeSpace() < 10000) {
                // 弹出对话框提示用户空间不够
                Log.e("Utils", "存储空间不够");
                return false;
            }

            // 在SDcard创建文件夹及文件
            File bitmapFile = new File(SDCardRoot.getPath() + filepath);
            bitmapFile.getParentFile().mkdirs();// 创建文件夹
            stream = new FileOutputStream(SDCardRoot.getPath() + filepath);// "/sdcard/"
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bmp.compress(format, quality, stream);
    }

    /**
     * 截屏
     *
     * @param activity
     * @return
     */
    public static Bitmap getScreenViewBitmap(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * 一个 View的图像
     *
     * @param view
     * @return
     */
    public static Bitmap getViewBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    public static Bitmap getUrlBitmap(String urlPath){
        return BitmapFactory.decodeFile(urlPath);
    }

    public static Bitmap getMatrixBitmap(Context context, String urlPath, Scene scene){
        Bitmap bitmap = getUrlBitmap(urlPath);
        if(bitmap == null || scene == null){
            return null;
        }
        Matrix matrix = new Matrix();
        float[] src = new float[] { 0, 0, // 左上
                bitmap.getWidth(), 0,// 右上
                bitmap.getWidth(), bitmap.getHeight(),// 右下
                0, bitmap.getHeight() };// 左下
        float[] dst = new float[] {
                scene.getLeftTop().x, scene.getLeftTop().y,
                scene.getRightTop().x, scene.getRightTop().y,
                scene.getRightBottom().x, scene.getRightBottom().y,
                scene.getLeftBottom().x, scene.getLeftBottom().y,
        };
        matrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1);
        Bitmap drawBitmap = BitmapFactory.decodeResource(context.getResources(),0);
        Bitmap alertBitmap=Bitmap.createBitmap(drawBitmap.getWidth(),
                drawBitmap.getHeight(), drawBitmap.getConfig());
        Canvas canvas=new Canvas(alertBitmap);
        Paint paint=new Paint();
        paint.setAntiAlias(true);
        canvas.drawColor(Color.BLACK);
        canvas.save();
        canvas.concat(matrix);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.restore();
        canvas.drawBitmap(drawBitmap, 0, 0, paint);
        return alertBitmap;
    }

    public static Bitmap getBitmapFromUri(String path, int displayWidth) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        float width =options.outWidth;
        int height =options.outHeight;
        float getMeasuredWidth = displayWidth;
        float sacle = getMeasuredWidth/width;
        if (new File(path).length() < BitmapUtils.MAX_SIZE){
            return resizeImage(BitmapFactory.decodeFile(path), (int)getMeasuredWidth-40,(int)(height*sacle)-40);
        }else{
            return resizeImage(getImageCompress(path), (int) getMeasuredWidth - 40, (int) (height * sacle) - 40);
        }
    }

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        return new BitmapDrawable(resizedBitmap).getBitmap();
    }
}
