package com.ekuater.admaker.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.ekuater.admaker.util.BmpUtils;

/**
 * Created by Administrator on 2015/6/10.
 */
public class CircleView extends View {

    private int mColor;
    private Paint mPaint;
    private float mWidth;

    public CircleView(Context context) {
        super(context);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int center = getWidth()/2;
        int innerCircle = BmpUtils.dp2px(getContext(), 83);
        int ringWidth = BmpUtils.dp2px(getContext(), 5);
        mPaint.setAntiAlias(true);
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(center, center,  center, mPaint);
    }

    public void setmColor(int color){
        mColor = color;
        invalidate();
    }
}
