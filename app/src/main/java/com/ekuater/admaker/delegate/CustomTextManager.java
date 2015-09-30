package com.ekuater.admaker.delegate;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Environment;

import com.ekuater.admaker.EnvConfig;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.Term;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Administrator on 2015/6/9.
 */
public class CustomTextManager extends BaseManager {

    public static final String sdUrl = Environment.getExternalStorageDirectory() + "/AdvertiseFont";

    private static CustomTextManager mCustomTextManager;

    public static synchronized void initInstance(Context context) {
        if (mCustomTextManager == null) {
            mCustomTextManager = new CustomTextManager(context.getApplicationContext());
        }
    }

    public static CustomTextManager getInstance(Context context) {
        if (mCustomTextManager == null) {
            initInstance(context);
        }
        return mCustomTextManager;
    }

    public CustomTextManager(Context context) {
        super(context);
    }

    public ArrayList<Term> getTermFont(Resources resources) {
        String[] strings = resources.getStringArray(R.array.term_font);
        ArrayList<Term> fonts = new ArrayList<>();
        if (strings != null) {
            for (int i = 0; i < strings.length; i++) {
                fonts.add(new Term(strings[i]));
            }
        }
        if (fonts.size() > 2) {
            fonts.add(1, new Term(""));
        }
        return fonts;
    }

    public ArrayList<Term> getTermColor(Resources resources) {
//        TypedArray typedArray = resources.obtainTypedArray(R.array.term_color);
        String[] strings = resources.getStringArray(R.array.term_color);
        ArrayList<Term> colors = null;
        if (strings != null) {
            colors = new ArrayList<>();
            for (int i = 0; i < strings.length; i++) {
                colors.add(new Term(strings[i], null));
            }
        }
        return colors;
    }

    public void copyBigDataToSD(Context context, String strOutFileName, String assetsUrl) throws IOException {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        myInput = context.getAssets().open(assetsUrl);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

    public void copyToSD(Context context, ArrayList<Term> fonts) {
        for (int i = 0; i < fonts.size(); i++) {
            String font = fonts.get(i).getFont();
            try {
                String url = EnvConfig.genFontFile().getPath() + "/";
                copyBigDataToSD(context, url + font, font);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getRectBitmap(Bitmap bit, int color) {
        Bitmap bitmap = Bitmap.createBitmap(280, 140, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawColor(color);
        canvas.save();

        Matrix matrix = new Matrix();
        matrix.setRectToRect(new RectF(0, 0, bit.getWidth(), bit.getHeight()),
                new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                Matrix.ScaleToFit.CENTER);
        canvas.drawBitmap(bit, matrix, paint);
        return bitmap;
    }

}
