package com.ekuater.admaker.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.ekuater.admaker.ui.util.ViewUtils;

/**
 * Created by Leo on 2015/8/14.
 *
 * @author Leo
 */
public class EditModeTextView extends VerticalTextView {

    private static final float MIN_TEXT_SIZE_DP = 4;

    private float mMinTextSize;

    public EditModeTextView(Context context) {
        super(context);
        init();
    }

    public EditModeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditModeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mMinTextSize = ViewUtils.dpToPx(MIN_TEXT_SIZE_DP, getContext());
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EditModeTextView(Context context, AttributeSet attrs,
                            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setTextSize(int unit, float size) {
        final float rawSize = TypedValue.applyDimension(unit, size,
                getContext().getResources().getDisplayMetrics());
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.max(rawSize, mMinTextSize));
    }

    public void onViewScale(float scale) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() * scale);
    }
}
