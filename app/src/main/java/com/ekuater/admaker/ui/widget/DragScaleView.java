package com.ekuater.admaker.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ekuater.admaker.ui.util.ViewUtils;

/**
 * Created by Leo on 2015/8/12.
 *
 * @author Leo
 */
public class DragScaleView extends View {

    private static final String TAG = DragScaleView.class.getSimpleName();

    private static final int TOP = 0x15;
    private static final int LEFT = 0x16;
    private static final int BOTTOM = 0x17;
    private static final int RIGHT = 0x18;
    private static final int LEFT_TOP = 0x11;
    private static final int RIGHT_TOP = 0x12;
    private static final int LEFT_BOTTOM = 0x13;
    private static final int RIGHT_BOTTOM = 0x14;
    private static final int CENTER = 0x19;

    protected int screenWidth;
    protected int screenHeight;
    protected float lastX;
    protected float lastY;
    protected Paint paint = new Paint();

    private int dragDirection;

    private static final int offset = 20;

    public DragScaleView(Context context) {
        super(context);
        init();
    }

    public DragScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DragScaleView(Context context, AttributeSet attrs,
                         int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        initScreenW_H();
    }

    /**
     * 初始化获取屏幕宽高
     */
    protected void initScreenW_H() {
        screenHeight = getResources().getDisplayMetrics().heightPixels - 40;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
    }

    private DrawFilter drawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.setDrawFilter(drawFilter);
        paint.setAntiAlias(false);

        paint.setColor(Color.RED);
        paint.setStrokeWidth(2.0f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(offset, offset, getWidth() - offset, getHeight()
                - offset, paint);

        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(getWidth() - offset * 2, getHeight() - offset * 2,
                getWidth(), getHeight(), paint);

        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(1.0f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(1, 1, getWidth() - 1, getHeight() - 1, paint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                break;
            default:
                dragDirection = 0;
                break;
        }

        lastY = (int) event.getRawY();
        lastX = (int) event.getRawX();

        return true;
    }

    private void onTouchDown(MotionEvent event) {
        dragDirection = getDirection(event.getX(), event.getY());
    }

    private void onTouchMove(MotionEvent event) {
        final float dx = event.getRawX() - lastX;
        final float dy = event.getRawY() - lastY;

        switch (dragDirection) {
            case RIGHT_BOTTOM: // 右下
                scaleAndRotate(dx, dy);
                break;
            case CENTER: // 点击中心-->>移动
            default:
                centerMove(dx, dy);
                break;
        }
    }

    /**
     * 触摸点为中心->>移动
     */
    private void centerMove(float dx, float dy) {
        setTranslationX(getTranslationX() + dx);
        setTranslationY(getTranslationY() + dy);
    }

    /**
     * scale and rotate
     */
    private void scaleAndRotate(float dx, float dy) {
        // Should be content bound
        final RectF boundRect = new RectF(offset, offset,
                getWidth() - offset, getHeight() - offset);
        // Should be rotate bound
        final RectF rotateRect = new RectF(getWidth() - offset * 2,
                getHeight() - offset * 2, getWidth(), getHeight());

        ViewUtils.rotateRect(rotateRect,
                new PointF(boundRect.centerX(), boundRect.centerY()),
                getRotation());

        final float xa = rotateRect.centerX() - boundRect.centerX();
        final float ya = rotateRect.centerY() - boundRect.centerY();
        final float xb = xa + dx;
        final float yb = ya + dy;
        final float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        final float curLen = (float) Math.sqrt(xb * xb + yb * yb);
        final float scale = curLen / srcLen; // 计算缩放比

        centerScaleRect(boundRect, scale);
        layoutOnSizeChanged(new PointF(boundRect.width() + offset * 2, boundRect.height() + offset * 2));

        final double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (-1 <= cos && cos <= 1) {
            // 拉普拉斯定理
            final float calMatrix = xa * yb - xb * ya;// 行列式计算 确定转动方向
            final int flag = calMatrix > 0 ? 1 : -1;
            final float angle = (float) Math.toDegrees(Math.acos(cos)) * flag;
            setRotation(getRotation() + angle);
        }
    }

    private void layoutOnSizeChanged(PointF size) {
        final float translateX = (getWidth() - size.x) / 2;
        final float translateY = (getHeight() - size.y) / 2;
        final ViewGroup.LayoutParams lp = getLayoutParams();

        if (lp != null) {
            lp.width = Math.round(size.x);
            lp.height = Math.round(size.y);
            setLayoutParams(lp);
            setTranslationX(getTranslationX() + translateX);
            setTranslationY(getTranslationY() + translateY);
        }
    }

    /**
     * 获取触摸点flag
     */
    protected int getDirection(float x, float y) {
        final int left = getLeft();
        final int right = getRight();
        final int bottom = getBottom();
        final int top = getTop();

        if (x < 40 && y < 40) {
            return LEFT_TOP;
        } else if (y < 40 && right - left - x < 40) {
            return RIGHT_TOP;
        } else if (x < 40 && bottom - top - y < 40) {
            return LEFT_BOTTOM;
        } else if (right - left - x < 40 && bottom - top - y < 40) {
            return RIGHT_BOTTOM;
        } else if (x < 40) {
            return LEFT;
        } else if (y < 40) {
            return TOP;
        } else if (right - left - x < 40) {
            return RIGHT;
        } else if (bottom - top - y < 40) {
            return BOTTOM;
        } else {
            return CENTER;
        }
    }

    private static void centerScaleRect(RectF rect, float scale) {
        final float w = rect.width();
        final float h = rect.height();
        final float newW = scale * w;
        final float newH = scale * h;
        final float dx = (newW - w) / 2;
        final float dy = (newH - h) / 2;

        rect.left -= dx;
        rect.top -= dy;
        rect.right += dx;
        rect.bottom += dy;
    }
}
