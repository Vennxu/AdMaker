package com.ekuater.admaker.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2015/2/7.
 *
 * @author Fan Chong
 */
public class ClickEventInterceptLinear extends LinearLayout {

    public ClickEventInterceptLinear(Context context) {
        super(context);
    }

    public ClickEventInterceptLinear(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickEventInterceptLinear(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
