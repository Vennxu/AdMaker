package com.ekuater.admaker.delegate;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.Scene;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/6/5.
 * @author Xu wenxiang
 */
public class SceneManager extends BaseManager{

    public volatile static SceneManager sInstance;

    public static synchronized void initInstance(Context context){
        if (sInstance == null){
            sInstance = new SceneManager(context.getApplicationContext());
        }
    }

    public static SceneManager getInstance(Context context){
        if (sInstance == null) {
            initInstance(context);
        }
        return sInstance;
    }

    public SceneManager(Context context){
        super(context);
    }

    public  ArrayList<Scene> getScenes(Resources res, String packageName) {
        final TypedArray array = res.obtainTypedArray(R.array.choose_image);
        ArrayList<Scene> scenes = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            Scene scene = new Scene();
            int arrayScene = array.getResourceId(i, 0);
            String[] arrayScenes = res.getStringArray(arrayScene);
//            scene.setImage(getResourceId(res,arrayScenes[0],packageName));
            scene.setLeftTop(new Point(Integer.parseInt(arrayScenes[1].split(",")[0]),
                    Integer.parseInt(arrayScenes[1].split(",")[1])));
            scene.setRightTop(new Point(Integer.parseInt(arrayScenes[2].split(",")[0]),
                    Integer.parseInt(arrayScenes[2].split(",")[1])));
            scene.setLeftBottom(new Point(Integer.parseInt(arrayScenes[3].split(",")[0]),
                    Integer.parseInt(arrayScenes[3].split(",")[1])));
            scene.setRightBottom(new Point(Integer.parseInt(arrayScenes[4].split(",")[0]),
                    Integer.parseInt(arrayScenes[4].split(",")[1])));
//            scene.setIsSelect(i == 0 ? Scene.SELECTED : Scene.NO_SELECTED);
            scenes.add(scene);
        }
        array.recycle();

        return scenes;
    }

    private int getResourceId(Resources res,String name, String packageName) {
        return res.getIdentifier(name, "drawable", packageName);
    }

    public Bitmap setSceneBitmap(Bitmap adBitmap, Scene scene, Bitmap baseSceneBitmap) {
        if (adBitmap == null || scene == null || baseSceneBitmap == null) {
            return null;
        }

        Matrix matrix = new Matrix();
        float[] src = new float[]{0, 0, // 左上
                adBitmap.getWidth(), 0,// 右上
                adBitmap.getWidth(), adBitmap.getHeight(),// 右下
                0, adBitmap.getHeight()};// 左下
        float[] dst = new float[]{
                scene.getLeftTop().x, scene.getLeftTop().y,
                scene.getRightTop().x, scene.getRightTop().y,
                scene.getRightBottom().x, scene.getRightBottom().y,
                scene.getLeftBottom().x, scene.getLeftBottom().y,
        };
        matrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1);
        Bitmap sceneBitmap = Bitmap.createBitmap(baseSceneBitmap.getWidth(),
                baseSceneBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(sceneBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.drawColor(Color.BLACK);
        canvas.save();
        canvas.concat(matrix);
        canvas.drawBitmap(adBitmap, 0, 0, paint);
        canvas.restore();
        canvas.drawBitmap(baseSceneBitmap, 0, 0, paint);
        return sceneBitmap;
    }



}
