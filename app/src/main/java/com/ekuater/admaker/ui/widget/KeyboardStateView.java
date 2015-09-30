package com.ekuater.admaker.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Leo on 2015/4/7.
 *
 * @author LinYong
 */
public class KeyboardStateView extends View {

    public static final byte KEYBOARD_STATE_INIT = -1;
    public static final byte KEYBOARD_STATE_HIDE = -2;
    public static final byte KEYBOARD_STATE_SHOW = -3;

    public interface OnKeyboardStateChangedListener {
        public void onKeyboardStateChanged(int state);
    }

    private interface ListenerNotifier {
        public void notify(OnKeyboardStateChangedListener listener);
    }

    private boolean mHasInit = false;
    private boolean mHasKeyboard = false;
    private int mHeight;
    private OnKeyboardStateChangedListener mKeyboardStateChangedListener;

    public KeyboardStateView(Context context) {
        super(context);
    }

    public KeyboardStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnKeyboardStateChangedListener(OnKeyboardStateChangedListener listener) {
        mKeyboardStateChangedListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!mHasInit) {
            mHasInit = true;
            mHeight = bottom;
            notifyKeyboardStateChanged(KEYBOARD_STATE_INIT);
        } else {
            mHeight = mHeight < bottom ? bottom : mHeight;
        }

        if (mHasInit && mHeight > bottom) {
            mHasKeyboard = true;
            notifyKeyboardStateChanged(KEYBOARD_STATE_SHOW);
        }

        if (mHasInit && mHasKeyboard && mHeight == bottom) {
            mHasKeyboard = false;
            notifyKeyboardStateChanged(KEYBOARD_STATE_HIDE);
        }
    }

    private void notifyListener(ListenerNotifier notifier) {
        if (mKeyboardStateChangedListener != null) {
            notifier.notify(mKeyboardStateChangedListener);
        }
    }

    private void notifyKeyboardStateChanged(final int state) {
        notifyListener(new ListenerNotifier() {
            @Override
            public void notify(OnKeyboardStateChangedListener listener) {
                listener.onKeyboardStateChanged(state);
            }
        });
    }
}
