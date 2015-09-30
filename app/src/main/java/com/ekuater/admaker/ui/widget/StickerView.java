package com.ekuater.admaker.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 贴图操作控件
 *
 * @author panyi
 */
public class StickerView extends View {

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_MOVE = 1;// 移动状态
    private static final int STATUS_DELETE = 2;// 删除状态
    private static final int STATUS_ROTATE = 3;// 图片旋转状态

    private int currentStatus;// 当前状态
    private StickerItem currentItem;// 当前操作的贴图数据
    private float oldX, oldY;
    private Paint rectPaint = new Paint();
    private List<StickerItem> bank = new ArrayList<>();// 存贮每层贴图数据
    private Point targetSize;

    public StickerView(Context context) {
        super(context);
        init();
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        currentStatus = STATUS_IDLE;
        rectPaint.setColor(Color.RED);
        rectPaint.setAlpha(100);
    }

    public void setTargetSize(Point size) {
        targetSize = size;
    }

    public void addStickerImage(@NonNull StickerType type,
                                @NonNull Bitmap bitmap, @Nullable Bitmap altBitmap) {
        if (targetSize == null) {
            return;
        }

        StickerItem item = new StickerItem(this, targetSize, type, bitmap, altBitmap);
        if (currentItem != null) {
            currentItem.isDrawHelpTool = false;
        }
        bank.add(item);
        currentItem = item;
        this.invalidate();// 重绘视图
    }

    /**
     * 绘制客户页面
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (StickerItem item : bank) {
            item.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        boolean ret = super.onTouchEvent(event);// 是否向下传递事件标志 true为消耗
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                StickerItem workItem = null;
                StickerItem deleteItem = null;
                StickerItem switchItem = null;

                for (int i = bank.size() - 1; i >= 0; --i) {
                    final StickerItem item = bank.get(i);

                    if (item.detectDeleteRect.contains(x, y)) {// 删除模式
                        deleteItem = item;
                        currentStatus = STATUS_DELETE;
                    } else if (item.detectRotateRect.contains(x, y)) {// 点击了旋转按钮
                        ret = true;
                        workItem = item;
                        currentStatus = STATUS_ROTATE;
                        oldX = x;
                        oldY = y;
                    } else if (item.touchInSwitchArea(x, y)) {
                        switchItem = item;
                        currentStatus = STATUS_MOVE;
                    } else if (item.dstRect.contains(x, y)) {// 移动模式
                        // 被选中一张贴图
                        ret = true;
                        workItem = item;
                        currentStatus = STATUS_MOVE;
                        oldX = x;
                        oldY = y;
                    } else {
                        continue;
                    }
                    break;
                }

                boolean needInvalidate = false;

                if (currentItem != null) {
                    currentItem.isDrawHelpTool = false;
                    currentItem = null;
                    needInvalidate = true;
                }

                if (workItem != null) {
                    workItem.isDrawHelpTool = true;
                    currentItem = workItem;
                    needInvalidate = true;
                } else if (deleteItem != null) {// 删除选定贴图
                    bank.remove(deleteItem);
                    currentStatus = STATUS_IDLE;// 返回空闲状态
                    deleteItem.recycle();
                    needInvalidate = true;
                } else if (switchItem != null) {
                    switchItem.switchBitmap();
                    needInvalidate = true;
                }

                if (needInvalidate) {
                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                ret = true;
                if (currentStatus == STATUS_MOVE) {// 移动贴图
                    float dx = x - oldX;
                    float dy = y - oldY;
                    if (currentItem != null) {
                        currentItem.updatePos(dx, dy);
                        invalidate();
                    }
                    oldX = x;
                    oldY = y;
                } else if (currentStatus == STATUS_ROTATE) {// 旋转 缩放图片操作
                    float dx = x - oldX;
                    float dy = y - oldY;
                    if (currentItem != null) {
                        currentItem.updateRotateAndScale(dx, dy);// 旋转
                        invalidate();
                    }
                    oldX = x;
                    oldY = y;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                ret = false;
                currentStatus = STATUS_IDLE;
                break;
            }
            default:
                break;
        }
        return ret;
    }

    public List<StickerItem> getBank() {
        return bank;
    }

    public void clear() {
        for (StickerItem item : bank) {
            item.recycle();
        }
        bank.clear();
        this.invalidate();
    }
}
