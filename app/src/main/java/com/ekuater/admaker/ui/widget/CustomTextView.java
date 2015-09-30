package com.ekuater.admaker.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2015/7/29.
 * @author Xu wenxiang
 */
public class CustomTextView extends VerticalTextView {

    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(l == null ? new CustomTextOnTouch() : l);
    }

    class CustomTextOnTouch implements OnTouchListener{
        int[] temp = new int[] { 0, 0 };
        Boolean ismove = false;
        int downX = 0;
        int downY = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eventaction = event.getAction();
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            switch (eventaction) {

                case MotionEvent.ACTION_DOWN: // touch down so check if the
                    temp[0] = (int) event.getX();
                    temp[1] = y - v.getTop();
                    downX = (int) event.getRawX();
                    downY = (int) event.getRawY();
                    ismove = false;
                    break;

                case MotionEvent.ACTION_MOVE: // touch drag with the ball
                    v.layout(x - temp[0], y - temp[1], x + v.getWidth() - temp[0], y - temp[1] + v.getHeight());
                    if (Math.abs(downX - x) > 10 || Math.abs(downY - y) > 10)
                        ismove = true;
                    break;

                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        }
    }
}
