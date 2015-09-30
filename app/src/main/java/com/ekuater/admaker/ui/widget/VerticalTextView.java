package com.ekuater.admaker.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/6/12.
 * Support horizontal & vertical text show
 *
 * @author LinYong
 */
public class VerticalTextView extends View {

    protected static class MeasureSize {

        public final Point measuredSize;
        public final Point desiredSize;

        public MeasureSize() {
            measuredSize = new Point();
            desiredSize = new Point();
        }
    }

    private enum Effect {
        Normal, Shadow, Stroke,
    }

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_TOP = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;
    public static final int ALIGN_BOTTOM = 2;

    private DrawFilter mDrawFilter;
    private TextPaint mTextPaint;
    private List<Layout> mLayouts;

    private int mOrientation = HORIZONTAL;
    private int mAlignment = ALIGN_LEFT;
    private int mDesiredWidth = 0;
    private int mDesiredHeight = 0;
    private Effect mEffect = Effect.Normal;

    // for Shadow effect
    private float mShadowRadius;
    private float mShadowDx;
    private float mShadowDy;
    private int mShadowColor = Color.BLACK;

    // For Stroke effect
    private float mStrokeWidth = 0;
    private int mStrokeColor = Color.BLACK;

    private Paint.Style mTempStyle;
    private float mTempStrokeWidth;
    private int mTempColor;
    private boolean mTempFakeBold;

    private String mText;

    public VerticalTextView(Context context) {
        super(context, null);
        init();
    }

    public VerticalTextView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public VerticalTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VerticalTextView(Context context, AttributeSet attrs,
                            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        final Resources res = getResources();
        mDrawFilter = new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mLayouts = new ArrayList<>();
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = res.getDisplayMetrics().density;
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        setTextColor(Color.WHITE);
    }

    /**
     * Sets the orientation of text display.
     * {@link #HORIZONTAL} or {@link #VERTICAL}
     *
     * @param orientation orientation
     */
    public void setOrientation(int orientation) {
        switch (orientation) {
            case HORIZONTAL:
            case VERTICAL:
                break;
            default:
                return;
        }

        if (mOrientation != orientation) {
            mOrientation = orientation;
            reParseLayouts();
            requestLayout();
            invalidate();
        }
    }

    public int getOrientation() {
        return mOrientation;
    }

    /**
     * Sets the typeface and style in which the text should be displayed,
     * and turns on the fake bold and italic bits in the Paint if the
     * Typeface that you provided does not have all the bits in the
     * style that you specified.
     */
    public void setTypeface(Typeface tf, int style) {
        if (style > 0) {
            if (tf == null) {
                tf = Typeface.defaultFromStyle(style);
            } else {
                tf = Typeface.create(tf, style);
            }

            setTypeface(tf);
            // now compute what (if any) algorithmic styling is needed
            int typefaceStyle = tf != null ? tf.getStyle() : 0;
            int need = style & ~typefaceStyle;
            mTextPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
            mTextPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
        } else {
            mTextPaint.setFakeBoldText(false);
            mTextPaint.setTextSkewX(0);
            setTypeface(tf);
        }
    }

    /**
     * Sets the typeface and style in which the text should be displayed.
     * Note that not all Typeface families actually have bold and italic
     * variants
     */
    public void setTypeface(Typeface tf) {
        if (mTextPaint.getTypeface() != tf) {
            mTextPaint.setTypeface(tf);
            reParseLayouts();
            requestLayout();
            invalidate();
        }
    }

    /**
     * Set the default text size to a given unit and value.  See {@link
     * TypedValue} for the possible dimension units.
     *
     * @param unit The desired dimension unit.
     * @param size The desired size in the given units.
     */
    public void setTextSize(int unit, float size) {
        float rawSize = TypedValue.applyDimension(unit, size,
                getContext().getResources().getDisplayMetrics());

        if (rawSize != mTextPaint.getTextSize()) {
            mTextPaint.setTextSize(rawSize);
            reParseLayouts();
            requestLayout();
            invalidate();
        }
    }

    /**
     * @return the size (in pixels) of the default text size in this TextView.
     */
    public float getTextSize() {
        return mTextPaint.getTextSize();
    }

    /**
     * Sets the text color
     */
    public void setTextColor(int color) {
        if (color != mTextPaint.getColor()) {
            mTextPaint.setColor(color);
            invalidate();
        }
    }

    /**
     * Sets text alignment
     * {@link #ALIGN_LEFT}, {@link #ALIGN_TOP}, {@link #ALIGN_CENTER},
     * {@link #ALIGN_RIGHT}, {@link #ALIGN_BOTTOM}
     *
     * @param align alignment
     */
    public void setAlignment(int align) {
        if (align != mAlignment) {
            mAlignment = align;
            invalidate();
        }
    }

    public void setEffectNormal() {
        mEffect = Effect.Normal;
        requestLayout();
        invalidate();
    }

    public void setEffectShadow(float radius, float dx, float dy) {
        mEffect = Effect.Shadow;
        mShadowRadius = radius;
        mShadowDx = dx;
        mShadowDy = dy;
        requestLayout();
        invalidate();
    }

    private void setupEffectShadow() {
        mTextPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
    }

    private void clearEffectShadow() {
        mTextPaint.clearShadowLayer();
    }

    public void setEffectStroke(float width) {
        mEffect = Effect.Stroke;
        mStrokeWidth = width;
        requestLayout();
        invalidate();
    }

    private void setupEffectStroke() {
        mTempStyle = mTextPaint.getStyle();
        mTempStrokeWidth = mTextPaint.getStrokeWidth();
        mTempColor = mTextPaint.getColor();
        mTempFakeBold = mTextPaint.isFakeBoldText();

        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setStrokeWidth(mStrokeWidth);
        mTextPaint.setColor(mStrokeColor);
        mTextPaint.setFakeBoldText(true);
    }

    private void clearEffectStroke() {
        mTextPaint.setStyle(mTempStyle);
        mTextPaint.setStrokeWidth(mTempStrokeWidth);
        mTextPaint.setColor(mTempColor);
        mTextPaint.setFakeBoldText(mTempFakeBold);
    }

    public void setEffectColor(int color) {
        mShadowColor = color;
        mStrokeColor = color;
        invalidate();
    }

    /**
     * Sets display text
     *
     * @param text text
     */
    public void setText(String text) {
        if (mText != null && mText.equals(text)) {
            return;
        }
        mText = text;
        parseLayouts(text);
        requestLayout();
        invalidate();
    }

    public String getText() {
        return mText;
    }

    public TextPaint getTextPaint() {
        return mTextPaint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        MeasureSize measureSize = doMeasureWithEffect(widthMeasureSpec, heightMeasureSpec);
        mDesiredWidth = measureSize.desiredSize.x;
        mDesiredHeight = measureSize.desiredSize.y;
        setMeasuredDimension(measureSize.measuredSize.x, measureSize.measuredSize.y);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        final int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.setDrawFilter(mDrawFilter);
        canvas.translate((getMeasuredWidth() - mDesiredWidth) / 2,
                (getMeasuredHeight() - mDesiredHeight) / 2);
        switch (mEffect) {
            case Shadow:
                setupEffectShadow();
                drawLayouts(canvas);
                clearEffectShadow();
                break;
            case Stroke:
                setupEffectStroke();
                drawLayouts(canvas);
                clearEffectStroke();
                drawLayouts(canvas);
                break;
            case Normal:
            default:
                drawLayouts(canvas);
                break;
        }
        canvas.restoreToCount(saveCount);
    }

    private void drawLayouts(Canvas canvas) {
        switch (mOrientation) {
            case HORIZONTAL:
                drawLayoutsHorizontal(canvas);
                break;
            case VERTICAL:
                drawLayoutsVertical(canvas);
                break;
            default:
                break;
        }
    }

    protected void reParseLayouts() {
        parseLayouts(mText);
    }

    private void parseLayouts(String text) {
        mLayouts.clear();

        if (TextUtils.isEmpty(text)) {
            return;
        }

        int start = 0;
        int length = text.length();
        for (int idx = 0; idx < length; ++idx) {
            char ch = text.charAt(idx);

            // make new line
            if (ch == '\n') {
                addLayout(makeNewLayout(text.substring(start, idx)));
                start = idx + 1;
            }
        }
        // make new line
        addLayout(makeNewLayout(start > length ? "" : text.substring(start, length)));
    }

    private void addLayout(Layout layout) {
        if (layout != null) {
            mLayouts.add(layout);
        }
    }

    private Layout makeNewLayout(@NonNull String text) {
        String layoutText;

        switch (mOrientation) {
            case HORIZONTAL:
                layoutText = text;
                break;
            case VERTICAL:
                layoutText = makeVerticalText(text);
                break;
            default:
                layoutText = null;
                break;
        }
        return (layoutText == null) ? null : new StaticLayout(layoutText, mTextPaint,
                (int) Math.ceil(Layout.getDesiredWidth(layoutText, mTextPaint)),
                Layout.Alignment.ALIGN_CENTER, 1, 0, true);
    }

    private String makeVerticalText(String text) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();

        for (int idx = 0; idx < length; ++idx) {
            char ch = text.charAt(idx);

            sb.append(ch);
            if (idx < length - 1 && ch != '\n') {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    @NonNull
    protected MeasureSize doMeasureWithEffect(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final MeasureSize measureSize = doMeasure(widthMeasureSpec, heightMeasureSpec);
        final int effectOffset = getEffectOffset();
        final int width = measureSize.measuredSize.x
                + ((widthMode == MeasureSpec.EXACTLY) ? 0 : effectOffset);
        final int height = measureSize.measuredSize.y
                + ((heightMode == MeasureSpec.EXACTLY) ? 0 : effectOffset);

        measureSize.measuredSize.set(width, height);
        return measureSize;
    }

    protected int getEffectOffset() {
        int effectOffset = 0;

        switch (mEffect) {
            case Shadow:
                effectOffset = Math.round(mShadowRadius * 2);
                break;
            case Stroke:
                effectOffset = Math.round(mStrokeWidth * 2);
                break;
            case Normal:
            default:
                break;
        }
        return effectOffset;
    }

    @NonNull
    protected MeasureSize doMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        return (mOrientation == VERTICAL)
                ? doVerticalMeasure(widthMeasureSpec, heightMeasureSpec)
                : doHorizontalMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @NonNull
    private MeasureSize doHorizontalMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        int desiredWidth = 0;
        int desiredHeight = 0;

        for (Layout layout : mLayouts) {
            desiredWidth = Math.max(desiredWidth, layout.getWidth());
            desiredHeight += layout.getHeight();
        }

        width = (widthMode == MeasureSpec.EXACTLY) ? widthSize : desiredWidth;
        height = (heightMode == MeasureSpec.EXACTLY) ? heightSize : desiredHeight;

        MeasureSize measureSize = new MeasureSize();
        measureSize.measuredSize.set(width, height);
        measureSize.desiredSize.set(desiredWidth, desiredHeight);
        return measureSize;
    }

    @NonNull
    private MeasureSize doVerticalMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        int desiredWidth = 0;
        int desiredHeight = 0;

        for (Layout layout : mLayouts) {
            desiredWidth += layout.getWidth();
            desiredHeight = Math.max(desiredHeight, layout.getHeight());
        }

        width = (widthMode == MeasureSpec.EXACTLY) ? widthSize : desiredWidth;
        height = (heightMode == MeasureSpec.EXACTLY) ? heightSize : desiredHeight;

        MeasureSize measureSize = new MeasureSize();
        measureSize.measuredSize.set(width, height);
        measureSize.desiredSize.set(desiredWidth, desiredHeight);
        return measureSize;
    }

    private void drawLayoutsHorizontal(Canvas canvas) {
        final int saveCount = canvas.getSaveCount();

        canvas.save();
        for (Layout layout : mLayouts) {
            float translateX;

            switch (mAlignment) {
                case ALIGN_CENTER:
                    translateX = (mDesiredWidth - layout.getWidth()) / 2;
                    break;
                case ALIGN_RIGHT:
                    translateX = (mDesiredWidth - layout.getWidth());
                    break;
                case ALIGN_LEFT:
                default:
                    translateX = 0;
                    break;
            }
            canvas.save();
            canvas.translate(translateX, 0);
            layout.draw(canvas);
            canvas.restore();
            canvas.translate(0, layout.getHeight());
        }
        canvas.restoreToCount(saveCount);
    }

    private void drawLayoutsVertical(Canvas canvas) {
        final int saveCount = canvas.getSaveCount();

        canvas.save();
        for (Layout layout : mLayouts) {
            float translateY;

            switch (mAlignment) {
                case ALIGN_CENTER:
                    translateY = (mDesiredHeight - layout.getHeight()) / 2;
                    break;
                case ALIGN_BOTTOM:
                    translateY = (mDesiredHeight - layout.getHeight());
                    break;
                case ALIGN_TOP:
                default:
                    translateY = 0;
                    break;
            }
            canvas.save();
            canvas.translate(0, translateY);
            layout.draw(canvas);
            canvas.restore();
            canvas.translate(layout.getWidth(), 0);
        }
        canvas.restoreToCount(saveCount);
    }
}
