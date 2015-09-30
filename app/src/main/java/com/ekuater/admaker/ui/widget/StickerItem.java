package com.ekuater.admaker.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.ekuater.admaker.R;

/**
 * @author panyi
 */
public class StickerItem {

    private static final float MIN_SCALE = 0.15f;
    private static final int HELP_BOX_PAD = 5;

    private volatile static boolean sInit = false;
    private static Bitmap deleteBmp;
    private static Bitmap rotateBmp;
    private static Bitmap switchBmp;
    private static Paint helpBoxPaint;
    private static int buttonSize;

    private static void staticInit(Context context) {
        if (sInit) {
            return;
        }
        staticInitInternal(context);
    }

    private static synchronized void staticInitInternal(Context context) {
        if (sInit) {
            return;
        }

        Resources res = context.getResources();

        helpBoxPaint = new Paint();
        helpBoxPaint.setColor(0xFFFFE0A2);
        helpBoxPaint.setStyle(Style.STROKE);
        helpBoxPaint.setAntiAlias(true);
        helpBoxPaint.setStrokeWidth(4);

        // 导入工具按钮位图
        if (deleteBmp == null) {
            deleteBmp = BitmapFactory.decodeResource(res,
                    R.drawable.sticker_delete);
        }
        if (rotateBmp == null) {
            rotateBmp = BitmapFactory.decodeResource(res,
                    R.drawable.sticker_rotate);
        }
        if (switchBmp == null) {
            switchBmp = BitmapFactory.decodeResource(res,
                    R.drawable.sticker_switch);
        }
        buttonSize = Math.round(rotateBmp.getWidth() / 2.0f);
        sInit = true;
    }

    public Matrix matrix;// 变化矩阵
    public RectF dstRect;// 绘制目标坐标
    public boolean isDrawHelpTool = false;
    public RectF detectRotateRect;
    public RectF detectDeleteRect;
    private RectF detectSwitchRect;
    public Bitmap curBitmap;
    private Point parentSize;
    private Point targetSize;
    private StickerType type;

    private Bitmap bitmap;
    private Bitmap altBitmap;
    private boolean canSwitch;
    private Rect helpToolsRect;
    private RectF helpBox;
    private RectF deleteRect;// 删除按钮位置
    private RectF rotateRect;// 旋转按钮位置
    private RectF switchRect;// 切换按钮位置
    private float rotateAngle = 0;
    private float initWidth;// 加入屏幕时原始宽度

    public StickerItem(@NonNull View parent, @NonNull Point targetSize,
                       @NonNull StickerType type,
                       @NonNull Bitmap bitmap, @Nullable Bitmap altBitmap) {
        staticInit(parent.getContext());
        init(parent, targetSize, type, bitmap, altBitmap);
    }

    private void init(View parent, Point targetSize, StickerType type,
                      Bitmap bitmap, Bitmap altBitmap) {
        this.parentSize = new Point(parent.getWidth(), parent.getHeight());
        this.targetSize = targetSize;
        this.type = type;
        this.bitmap = bitmap;
        this.altBitmap = altBitmap;
        this.canSwitch = altBitmap != null;
        this.helpToolsRect = new Rect(0, 0, deleteBmp.getWidth(),
                deleteBmp.getHeight());
        switchCurrentBitmap(this.bitmap);
    }

    private void switchCurrentBitmap(Bitmap bitmap) {
        if (bitmap == curBitmap) {
            return;
        }

        final float scale = getInitScale(bitmap);
        this.curBitmap = bitmap;
        this.dstRect = newInitDstRect(bitmap, scale);
        this.rotateAngle = 0;
        this.matrix = newInitMatrix(this.dstRect, scale);
        this.initWidth = this.dstRect.width();// 记录原始宽度
        this.isDrawHelpTool = true;
        this.helpBox = new RectF(this.dstRect);
        updateHelpBoxRect();
        this.deleteRect = new RectF(helpBox.left - buttonSize, helpBox.top - buttonSize,
                helpBox.left + buttonSize, helpBox.top + buttonSize);
        this.rotateRect = new RectF(helpBox.right - buttonSize, helpBox.bottom - buttonSize,
                helpBox.right + buttonSize, helpBox.bottom + buttonSize);
        this.switchRect = new RectF(helpBox.right - buttonSize, helpBox.top - buttonSize,
                helpBox.right + buttonSize, helpBox.top + buttonSize);
        this.detectRotateRect = new RectF(rotateRect);
        this.detectDeleteRect = new RectF(deleteRect);
        this.detectSwitchRect = new RectF(switchRect);
    }

    private Matrix newInitMatrix(RectF dstRect, float scale) {
        Matrix matrix = new Matrix();
        matrix.postTranslate(dstRect.left, dstRect.top);
        matrix.postScale(scale, scale, dstRect.left, dstRect.top);
        return matrix;
    }

    private float getInitScale(Bitmap bitmap) {
        final float scale;

        switch (this.type) {
            case SLOGAN:
                scale = (this.targetSize.y / 7.0F)
                        / Math.min(bitmap.getHeight(), bitmap.getWidth());
                break;
            case TRADEMARK:
                scale = (this.targetSize.y / 5.0F) / bitmap.getHeight();
                break;
            default:
                scale = 1.0F;
                break;
        }
        return scale;
    }

    private RectF newInitDstRect(Bitmap bitmap, float scale) {
        final RectF dstRect = new RectF(0, 0, bitmap.getWidth() * scale,
                bitmap.getHeight() * scale);
        final float offsetGap = HELP_BOX_PAD + buttonSize;

        switch (this.type) {
            case SLOGAN:
                dstRect.offsetTo(this.targetSize.x - dstRect.width(), 0);
                dstRect.offset(-offsetGap, offsetGap);
                break;
            case TRADEMARK:
                dstRect.offsetTo(0, this.targetSize.y - dstRect.height());
                dstRect.offset(offsetGap, -offsetGap);
                break;
            default:
                dstRect.offset((this.targetSize.x - dstRect.width()) / 2,
                        (this.targetSize.y - dstRect.height()) / 2);
                break;
        }
        dstRect.offset((this.parentSize.x - this.targetSize.x) / 2.0F,
                (this.parentSize.y - this.targetSize.y) / 2.0F);
        return dstRect;
    }

    private void updateHelpBoxRect() {
        this.helpBox.left -= HELP_BOX_PAD;
        this.helpBox.right += HELP_BOX_PAD;
        this.helpBox.top -= HELP_BOX_PAD;
        this.helpBox.bottom += HELP_BOX_PAD;
    }

    public void switchBitmap() {
        Bitmap tmpBmp = (curBitmap == bitmap) ? altBitmap : bitmap;
        if (tmpBmp != null) {
            switchCurrentBitmap(tmpBmp);
        }
    }

    public boolean touchInSwitchArea(float x, float y) {
        return canSwitch && detectSwitchRect.contains(x, y);
    }

    /**
     * 位置更新
     */
    public void updatePos(final float dx, final float dy) {
        matrix.postTranslate(dx, dy);// 记录到矩阵中
        dstRect.offset(dx, dy);
        // 工具按钮随之移动
        helpBox.offset(dx, dy);
        deleteRect.offset(dx, dy);
        rotateRect.offset(dx, dy);
        switchRect.offset(dx, dy);
        detectRotateRect.offset(dx, dy);
        detectDeleteRect.offset(dx, dy);
        detectSwitchRect.offset(dx, dy);
    }

    /**
     * 旋转 缩放 更新
     */
    public void updateRotateAndScale(final float dx, final float dy) {
        float c_x = dstRect.centerX();
        float c_y = dstRect.centerY();

        float x = this.detectRotateRect.centerX();
        float y = this.detectRotateRect.centerY();

        float n_x = x + dx;
        float n_y = y + dy;

        float xa = x - c_x;
        float ya = y - c_y;

        float xb = n_x - c_x;
        float yb = n_y - c_y;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);
        float scale = curLen / srcLen;// 计算缩放比

        float newWidth = dstRect.width() * scale;
        if (newWidth / initWidth < MIN_SCALE) {// 最小缩放值检测
            return;
        }

        this.matrix.postScale(scale, scale, this.dstRect.centerX(),
                this.dstRect.centerY());// 存入scale矩阵
        scaleRect(this.dstRect, scale);// 缩放目标矩形

        // 重新计算工具箱坐标
        helpBox.set(dstRect);
        updateHelpBoxRect();// 重新计算
        rotateRect.offsetTo(helpBox.right - buttonSize, helpBox.bottom - buttonSize);
        deleteRect.offsetTo(helpBox.left - buttonSize, helpBox.top - buttonSize);
        switchRect.offsetTo(helpBox.right - buttonSize, helpBox.top - buttonSize);
        detectRotateRect.offsetTo(rotateRect.left, rotateRect.top);
        detectDeleteRect.offsetTo(deleteRect.left, deleteRect.top);
        detectSwitchRect.offsetTo(switchRect.left, switchRect.top);

        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1) {
            return;
        }

        float angle = (float) Math.toDegrees(Math.acos(cos));
        // 拉普拉斯定理
        float calMatrix = xa * yb - xb * ya;// 行列式计算 确定转动方向
        int flag = calMatrix > 0 ? 1 : -1;

        angle = flag * angle;
        rotateAngle += angle;
        matrix.postRotate(angle, dstRect.centerX(), dstRect.centerY());
        rotateRect(detectRotateRect, dstRect.centerX(), dstRect.centerY(), rotateAngle);
        rotateRect(detectDeleteRect, dstRect.centerX(), dstRect.centerY(), rotateAngle);
        rotateRect(detectSwitchRect, dstRect.centerX(), dstRect.centerY(), rotateAngle);
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(curBitmap, matrix, null);// 贴图元素绘制

        if (isDrawHelpTool) {// 绘制辅助工具线
            canvas.save();
            canvas.rotate(rotateAngle, helpBox.centerX(), helpBox.centerY());
            canvas.drawRoundRect(helpBox, 10, 10, helpBoxPaint);
            // 绘制工具按钮
            canvas.drawBitmap(deleteBmp, helpToolsRect, deleteRect, null);
            canvas.drawBitmap(rotateBmp, helpToolsRect, rotateRect, null);
            if (canSwitch) {
                canvas.drawBitmap(switchBmp, helpToolsRect, switchRect, null);
            }
            canvas.restore();
        }
    }

    public void recycle() {
        // do nothing
    }

    /**
     * 缩放指定矩形
     *
     * @param rect  rect
     * @param scale scale
     */
    private static void scaleRect(RectF rect, float scale) {
        float w = rect.width();
        float h = rect.height();

        float newW = scale * w;
        float newH = scale * h;

        float dx = (newW - w) / 2;
        float dy = (newH - h) / 2;

        rect.left -= dx;
        rect.top -= dy;
        rect.right += dx;
        rect.bottom += dy;
    }

    /**
     * 矩形绕指定点旋转
     *
     * @param rect        rect
     * @param center_x    center_x
     * @param center_y    center_y
     * @param rotateAngle rotateAngle
     */
    private static void rotateRect(RectF rect, float center_x, float center_y,
                                   float rotateAngle) {
        float x = rect.centerX();
        float y = rect.centerY();
        float sinA = (float) Math.sin(Math.toRadians(rotateAngle));
        float cosA = (float) Math.cos(Math.toRadians(rotateAngle));
        float newX = center_x + (x - center_x) * cosA - (y - center_y) * sinA;
        float newY = center_y + (y - center_y) * cosA + (x - center_x) * sinA;
        float dx = newX - x;
        float dy = newY - y;
        rect.offset(dx, dy);
    }
}
