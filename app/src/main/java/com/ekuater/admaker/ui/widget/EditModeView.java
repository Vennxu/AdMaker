package com.ekuater.admaker.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ekuater.admaker.R;
import com.ekuater.admaker.ui.util.CompatUtils;
import com.ekuater.admaker.ui.util.ViewUtils;

/**
 * Created by Leo on 2015/8/12.
 *
 * @author Leo
 */
public class EditModeView extends View implements View.OnLayoutChangeListener {

    private static final float DEFAULT_BUTTON_SIZE_DP = 10;
    private static final float DEFAULT_STROKE_WIDTH_DP = 1;
    private static final int WRAP_PAINT_COLOR = Color.GREEN;
    private static final int ATTACH_PAINT_COLOR = Color.CYAN;

    public interface EventListener {

        void onAttachViewDelete(View attachView);

        void onAttachViewScale(View attachView, float scale);

        boolean onAttachViewSingleTapUp(View attachView);

        boolean onAttachViewDoubleTap(View attachView);
    }

    private interface IListenerNotifier {

        void notify(@NonNull EventListener listener);
    }

    private enum TouchAction {
        NONE,
        MOVE,
        DELETE,
        ROTATE_AND_SCALE
    }

    private Drawable mRotateDrawable;
    private Drawable mDeleteDrawable;
    private int mButtonSize;
    private Point mMinWrapSize;

    private DrawFilter mDrawFilter;
    private Rect mWrapBounds;
    private Paint mWrapPaint;

    private View mAttachView;
    private Rect mAttachBounds;
    private Paint mAttachPaint;

    private TouchAction mTouchAction;
    private PointF mLastRawTouchPoint;

    private EventListener mEventListener;
    private GestureDetector mGestureDetector;
    private boolean mDeletable;

    private final GestureDetector.SimpleOnGestureListener mGestureListener
            = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return onGestureSingleTapUp();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return onGestureDoubleTap();
        }
    };

    public EditModeView(Context context) {
        super(context);
        init();
    }

    public EditModeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditModeView(Context context, AttributeSet attrs,
                        int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EditModeView(Context context, AttributeSet attrs,
                        int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mRotateDrawable = getDrawable(R.drawable.sticker_rotate);
        mDeleteDrawable = getDrawable(R.drawable.sticker_delete);

        int size = Math.max(getDrawableSize(mRotateDrawable),
                getDrawableSize(mDeleteDrawable));
        mButtonSize = size > 0 ? size : dpToPx(DEFAULT_BUTTON_SIZE_DP);
        Rect bounds = new Rect(0, 0, mButtonSize, mButtonSize);
        mMinWrapSize = new Point(mButtonSize, mButtonSize);

        mDrawFilter = new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        mWrapBounds = new Rect(0, 0, mMinWrapSize.x, mMinWrapSize.y);
        mWrapPaint = new Paint();
        mWrapPaint.setColor(WRAP_PAINT_COLOR);
        mWrapPaint.setStrokeWidth(dpToPx(DEFAULT_STROKE_WIDTH_DP));
        mWrapPaint.setStyle(Paint.Style.STROKE);
        mWrapPaint.setPathEffect(new DashPathEffect(new float[]{dpToPx(5), dpToPx(8)}, 0));

        mAttachBounds = new Rect();
        mAttachPaint = new Paint();
        mAttachPaint.setColor(ATTACH_PAINT_COLOR);
        mAttachPaint.setStrokeWidth(dpToPx(DEFAULT_STROKE_WIDTH_DP));
        mAttachPaint.setStyle(Paint.Style.STROKE);

        mRotateDrawable.setBounds(bounds);
        mDeleteDrawable.setBounds(bounds);
        mWrapBounds.offsetTo(mButtonSize / 2, mButtonSize / 2);

        mTouchAction = TouchAction.NONE;
        mLastRawTouchPoint = new PointF();

        mGestureDetector = new GestureDetector(getContext(), mGestureListener);
        mGestureDetector.setOnDoubleTapListener(mGestureListener);
        mDeletable = true;
        setMinimumWidth(mMinWrapSize.x + mButtonSize);
        setMinimumHeight(mMinWrapSize.y + mButtonSize);
    }

    public void setEditListener(EventListener listener) {
        mEventListener = listener;
    }

    public void setDeletable(boolean deletable) {
        mDeletable = deletable;
        invalidate();
    }

    public void attachToView(View view) {
        detachFromView();
        mAttachView = view;
        mAttachView.addOnLayoutChangeListener(this);
        addToParent();
        updateRotationFromAttachView();
        updateWrapBoundsFromAttachView();
    }

    public void detachFromView() {
        if (mAttachView != null) {
            mAttachView.removeOnLayoutChangeListener(this);
            mAttachView = null;
        }
        removeFromParent();
    }

    public View getAttachView() {
        return mAttachView;
    }

    private void addToParent() {
        if (mAttachView != null) {
            ViewParent parent = mAttachView.getParent();
            if (parent != null && parent instanceof ViewGroup) {
                ((ViewGroup) parent).addView(this);
            }
        }
    }

    private void removeFromParent() {
        ViewParent parent = getParent();
        if (parent != null && parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(this);
        }
    }

    private void updateWrapBoundsFromAttachView() {
        if (mAttachView != null) {
            mAttachBounds.set(0, 0, mAttachView.getWidth(), mAttachView.getHeight());

            final int width = Math.max(mMinWrapSize.x, mAttachBounds.width()) + mButtonSize;
            final int height = Math.max(mMinWrapSize.y, mAttachBounds.height()) + mButtonSize;
            mAttachBounds.offsetTo((width - mAttachBounds.width()) / 2,
                    (height - mAttachBounds.height()) / 2);
            updateTranslationFromAttachView(width, height);
            updateViewSize(width, height);
        }
    }

    private void updateTranslationFromAttachView(final int newWidth, final int newHeight) {
        if (mAttachView != null) {
            final float attachViewX = mAttachView.getLeft() + mAttachView.getTranslationX();
            final float attachViewY = mAttachView.getTop() + mAttachView.getTranslationY();
            setTranslationX(attachViewX - (newWidth - mAttachBounds.width()) / 2);
            setTranslationY(attachViewY - (newHeight - mAttachBounds.height()) / 2);
        }
    }

    private void updateViewSize(int width, int height) {
        final ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp != null) {
            lp.width = width;
            lp.height = height;
            setLayoutParams(lp);
        }
    }

    private void updateRotationFromAttachView() {
        if (mAttachView != null) {
            setRotation(mAttachView.getRotation());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            final int width = right - left;
            final int height = bottom - top;

            Rect bounds;
            bounds = mRotateDrawable.copyBounds();
            bounds.offsetTo(width - bounds.width(), height - bounds.height());
            mRotateDrawable.setBounds(bounds);
            bounds = mDeleteDrawable.copyBounds();
            bounds.offsetTo(0, 0);
            mDeleteDrawable.setBounds(bounds);

            mWrapBounds.set(0, 0, width - mButtonSize, height - mButtonSize);
            mWrapBounds.offsetTo(mButtonSize / 2, mButtonSize / 2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.setDrawFilter(mDrawFilter);

        canvas.drawRect(mWrapBounds, mWrapPaint);
        canvas.drawRect(mAttachBounds, mAttachPaint);

        mRotateDrawable.draw(canvas);
        if (mDeletable) {
            mDeleteDrawable.draw(canvas);
        }

        canvas.restoreToCount(saveCount);
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
            case MotionEvent.ACTION_UP:
                onTouchUp(event);
                break;
            default:
                onTouchDone();
                break;
        }
        return mGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void onTouchDown(MotionEvent event) {
        final int x = Math.round(event.getX());
        final int y = Math.round(event.getY());

        mLastRawTouchPoint.set(event.getRawX(), event.getRawY());

        // Get touch action
        if (mDeletable && mDeleteDrawable.getBounds().contains(x, y)) {
            mTouchAction = TouchAction.DELETE;
        } else if (mRotateDrawable.getBounds().contains(x, y)) {
            mTouchAction = TouchAction.ROTATE_AND_SCALE;
        } else if (mWrapBounds.contains(x, y)) {
            mTouchAction = TouchAction.MOVE;
        } else {
            mTouchAction = TouchAction.NONE;
        }
    }

    private void onTouchMove(MotionEvent event) {
        final float rawX = event.getRawX();
        final float rawY = event.getRawY();
        final float dx = rawX - mLastRawTouchPoint.x;
        final float dy = rawY - mLastRawTouchPoint.y;

        mLastRawTouchPoint.set(rawX, rawY);

        switch (mTouchAction) {
            case ROTATE_AND_SCALE:
                doRotateAndScaleAction(dx, dy);
                break;
            case MOVE:
                doMoveAction(dx, dy);
                break;
            default:
                break;
        }
    }

    private void doRotateAndScaleAction(final float dx, final float dy) {
        final RectF wrapBounds = new RectF(mWrapBounds);
        final RectF rotateBounds = new RectF(mRotateDrawable.getBounds());

        ViewUtils.rotateRect(rotateBounds,
                new PointF(wrapBounds.centerX(), wrapBounds.centerY()),
                getRotation());

        final float xa = rotateBounds.centerX() - wrapBounds.centerX();
        final float ya = rotateBounds.centerY() - wrapBounds.centerY();
        final float xb = xa + dx;
        final float yb = ya + dy;
        final float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        final float curLen = (float) Math.sqrt(xb * xb + yb * yb);
        final float scale = curLen / srcLen;

        notifyAttachViewScale(scale);

        final double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (-1 <= cos && cos <= 1) {
            // 拉普拉斯定理
            final float calMatrix = xa * yb - xb * ya;// 行列式计算 确定转动方向
            final int flag = calMatrix > 0 ? 1 : -1;
            final float angle = (float) Math.toDegrees(Math.acos(cos)) * flag;
            final float rotation = getRotation() + angle;
            setRotation(rotation);
            mAttachView.setRotation(rotation);
        }
    }

    private void doMoveAction(final float dx, final float dy) {
        setTranslationX(getTranslationX() + dx);
        setTranslationY(getTranslationY() + dy);

        if (mAttachView != null) {
            mAttachView.setTranslationX(mAttachView.getTranslationX() + dx);
            mAttachView.setTranslationY(mAttachView.getTranslationY() + dy);
        }
    }

    private void onTouchUp(MotionEvent event) {
        final int x = Math.round(event.getX());
        final int y = Math.round(event.getY());

        switch (mTouchAction) {
            case DELETE:
                if (mDeleteDrawable.getBounds().contains(x, y)) {
                    notifyAttachViewDelete();
                }
                break;
            default:
                break;
        }

        onTouchDone();
    }

    private void onTouchDone() {
        mTouchAction = TouchAction.NONE;
        mLastRawTouchPoint.set(0, 0);
    }

    private boolean onGestureSingleTapUp() {
        return mAttachView != null && mEventListener != null
                && mEventListener.onAttachViewSingleTapUp(mAttachView);
    }

    private boolean onGestureDoubleTap() {
        return mAttachView != null && mEventListener != null
                && mEventListener.onAttachViewDoubleTap(mAttachView);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (mAttachView == v) {
            post(new Runnable() {
                @Override
                public void run() {
                    updateWrapBoundsFromAttachView();
                }
            });
        }
    }

    private void notifyAttachViewDelete() {
        notifyEditListener(new IListenerNotifier() {
            @Override
            public void notify(@NonNull EventListener listener) {
                if (mAttachView != null) {
                    listener.onAttachViewDelete(mAttachView);
                }
            }
        });
    }

    private void notifyAttachViewScale(final float scale) {
        notifyEditListener(new IListenerNotifier() {
            @Override
            public void notify(@NonNull EventListener listener) {
                if (mAttachView != null) {
                    listener.onAttachViewScale(mAttachView, scale);
                }
            }
        });
    }

    private void notifyEditListener(IListenerNotifier notifier) {
        if (mEventListener != null) {
            notifier.notify(mEventListener);
        }
    }

    private Drawable getDrawable(final int id) {
        return CompatUtils.getDrawable(getContext(), id);
    }

    private int getDrawableSize(Drawable drawable) {
        return Math.max(drawable.getMinimumHeight(), drawable.getMinimumWidth());
    }

    private int dpToPx(final float dp) {
        return ViewUtils.dpToPx(dp, getContext());
    }
}
