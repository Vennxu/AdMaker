package com.ekuater.admaker.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.ekuater.admaker.R;

/**
 * Created by Leo on 2015/7/11.
 *
 * @author Leo
 */
public class SendingProgressView extends View {

    public static final int STATE_NOT_STARTED = 0;
    public static final int STATE_PROGRESS_STARTED = 1;
    public static final int STATE_DONE_STARTED = 2;
    public static final int STATE_FINISHED = 3;

    private static final int FINISH_SUCCESS_COLOR = 0XFF39CB72;
    private static final int FINISH_FAILED_COLOR = 0XFFFF3030;

    private static final int PROGRESS_STROKE_SIZE = 10;
    private static final int INNER_CIRCLE_PADDING = 30;
    private static final int MAX_DONE_BG_OFFSET = 800;
    private static final int MAX_DONE_IMG_OFFSET = 400;

    private int state = STATE_NOT_STARTED;
    private boolean autoProgress = true;
    private float currentProgress = 0;
    private float currentDoneBgOffset = MAX_DONE_BG_OFFSET;
    private float currentCheckmarkOffset = MAX_DONE_IMG_OFFSET;
    private boolean finishSuccess = true;

    private Paint progressPaint;
    private Paint doneBgPaint;
    private Paint maskPaint;

    private RectF progressBounds;

    private Bitmap checkmarkBitmap;
    private Bitmap innerCircleMaskBitmap;

    private int checkmarkXPosition = 0;
    private int checkmarkYPosition = 0;

    private Paint checkmarkPaint;
    private Bitmap tempBitmap;
    private Canvas tempCanvas;

    private ObjectAnimator simulateProgressAnimator;
    private ObjectAnimator doneBgAnimator;
    private ObjectAnimator checkmarkAnimator;

    private OnLoadingFinishedListener onLoadingFinishedListener;

    public SendingProgressView(Context context) {
        super(context);
        init();
    }

    public SendingProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SendingProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SendingProgressView(Context context, AttributeSet attrs,
                               int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setupProgressPaint();
        setupDonePaints();
        setupSimulateProgressAnimator();
        setupDoneAnimators();
    }

    private void setupProgressPaint() {
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(0xffffffff);
        progressPaint.setStrokeWidth(PROGRESS_STROKE_SIZE);
    }

    private void setupSimulateProgressAnimator() {
        simulateProgressAnimator = ObjectAnimator.ofFloat(this, "currentProgress", 0, 100).setDuration(2000);
        simulateProgressAnimator.setInterpolator(new AccelerateInterpolator());
        simulateProgressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                changeState(STATE_DONE_STARTED);
            }
        });
    }

    private void setupDonePaints() {
        doneBgPaint = new Paint();
        doneBgPaint.setAntiAlias(true);
        doneBgPaint.setStyle(Paint.Style.FILL);
        doneBgPaint.setColor(FINISH_SUCCESS_COLOR);

        checkmarkPaint = new Paint();

        maskPaint = new Paint();
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    private void setupDoneAnimators() {
        doneBgAnimator = ObjectAnimator.ofFloat(this, "currentDoneBgOffset", MAX_DONE_BG_OFFSET, 0).setDuration(300);
        doneBgAnimator.setInterpolator(new DecelerateInterpolator());

        checkmarkAnimator = ObjectAnimator.ofFloat(this, "currentCheckmarkOffset", MAX_DONE_IMG_OFFSET, 0).setDuration(300);
        checkmarkAnimator.setInterpolator(new OvershootInterpolator());
        checkmarkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                changeState(STATE_FINISHED);
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateProgressBounds();
        setupCheckmarkBitmap();
        setupDoneMaskBitmap();
        resetTempCanvas();
    }

    private void updateProgressBounds() {
        progressBounds = new RectF(
                PROGRESS_STROKE_SIZE, PROGRESS_STROKE_SIZE,
                getWidth() - PROGRESS_STROKE_SIZE, getWidth() - PROGRESS_STROKE_SIZE
        );
    }

    private void setupCheckmarkBitmap() {
        if (checkmarkBitmap != null) {
            checkmarkBitmap.recycle();
        }
        checkmarkBitmap = BitmapFactory.decodeResource(getResources(), this.finishSuccess
                ? R.drawable.ic_done_white_48dp : R.drawable.ic_close_white_48dp);
        checkmarkXPosition = getWidth() / 2 - checkmarkBitmap.getWidth() / 2;
        checkmarkYPosition = getWidth() / 2 - checkmarkBitmap.getHeight() / 2;
    }

    private void setupDoneMaskBitmap() {
        innerCircleMaskBitmap = Bitmap.createBitmap(getWidth(), getWidth(), Bitmap.Config.ARGB_8888);
        Canvas srcCanvas = new Canvas(innerCircleMaskBitmap);
        srcCanvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2 - INNER_CIRCLE_PADDING, new Paint());
    }

    private void resetTempCanvas() {
        if (tempBitmap != null) {
            tempBitmap.recycle();
        }
        tempBitmap = Bitmap.createBitmap(getWidth(), getWidth(), Bitmap.Config.ARGB_8888);
        tempCanvas = new Canvas(tempBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (state == STATE_PROGRESS_STARTED) {
            drawArcForCurrentProgress();
        } else if (state == STATE_DONE_STARTED) {
            drawFrameForDoneAnimation();
            postInvalidate();
        } else if (state == STATE_FINISHED) {
            drawFinishedState();
        }

        canvas.drawBitmap(tempBitmap, 0, 0, null);
    }

    private void drawArcForCurrentProgress() {
        tempCanvas.drawArc(progressBounds, -90f, 360 * currentProgress / 100, false, progressPaint);
    }

    private void drawFrameForDoneAnimation() {
        tempCanvas.drawCircle(getWidth() / 2, getWidth() / 2 + currentDoneBgOffset, getWidth() / 2 - INNER_CIRCLE_PADDING, doneBgPaint);
        tempCanvas.drawBitmap(checkmarkBitmap, checkmarkXPosition, checkmarkYPosition + currentCheckmarkOffset, checkmarkPaint);
        tempCanvas.drawBitmap(innerCircleMaskBitmap, 0, 0, maskPaint);
        tempCanvas.drawArc(progressBounds, 0, 360f, false, progressPaint);
    }

    private void drawFinishedState() {
        tempCanvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2 - INNER_CIRCLE_PADDING, doneBgPaint);
        tempCanvas.drawBitmap(checkmarkBitmap, checkmarkXPosition, checkmarkYPosition, checkmarkPaint);
        tempCanvas.drawArc(progressBounds, 0, 360f, false, progressPaint);
    }

    private void changeState(int state) {
        if (this.state == state) {
            return;
        }

        resetTempCanvas();

        this.state = state;
        if (state == STATE_PROGRESS_STARTED) {
            setCurrentProgress(0);
            if (autoProgress) {
                simulateProgressAnimator.start();
            }
        } else if (state == STATE_DONE_STARTED) {
            setCurrentDoneBgOffset(MAX_DONE_BG_OFFSET);
            setCurrentCheckmarkOffset(MAX_DONE_IMG_OFFSET);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(doneBgAnimator, checkmarkAnimator);
            animatorSet.start();
        } else if (state == STATE_FINISHED) {
            if (onLoadingFinishedListener != null) {
                onLoadingFinishedListener.onLoadingFinished();
            }
        }
    }

    public void simulateProgress() {
        setAutoProgress(true);
        setFinishSuccessInternal(true);
        changeState(STATE_PROGRESS_STARTED);
    }

    private void setAutoProgress(boolean autoProgress) {
        this.autoProgress = autoProgress;
    }

    public void startProgress() {
        setAutoProgress(false);
        setFinishSuccessInternal(true);
        startProgress(0.0f);
    }

    public void startProgress(float initProgress) {
        changeState(STATE_PROGRESS_STARTED);
        setCurrentProgress(initProgress);
    }

    public void setCurrentProgress(float currentProgress) {
        this.currentProgress = Math.min(Math.max(0.0f, currentProgress), 100.0f);
        if (this.state == STATE_PROGRESS_STARTED) {
            postInvalidate();
        }
    }

    public void finishProgress(boolean success) {
        if (!this.autoProgress && this.state == STATE_PROGRESS_STARTED) {
            setFinishSuccessInternal(success);
            changeState(STATE_DONE_STARTED);
        }
    }

    public void setFinishSuccessInternal(boolean finishSuccess) {
        this.finishSuccess = finishSuccess;
        this.doneBgPaint.setColor(finishSuccess ? FINISH_SUCCESS_COLOR : FINISH_FAILED_COLOR);
        setupCheckmarkBitmap();
    }

    public void setCurrentDoneBgOffset(float currentDoneBgOffset) {
        this.currentDoneBgOffset = currentDoneBgOffset;
        postInvalidate();
    }

    public void setCurrentCheckmarkOffset(float currentCheckmarkOffset) {
        this.currentCheckmarkOffset = currentCheckmarkOffset;
        postInvalidate();
    }

    public void setOnLoadingFinishedListener(OnLoadingFinishedListener onLoadingFinishedListener) {
        this.onLoadingFinishedListener = onLoadingFinishedListener;
    }

    public interface OnLoadingFinishedListener {
        void onLoadingFinished();
    }
}
