package com.ekuater.admaker.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Leo on 2015/8/12.
 *
 * @author Leo
 */
public class TemplateLayout extends FrameLayout {

    public interface EventListener {

        void onViewAttached(View view);

        void onViewDetached(View view);

        boolean onActiveViewSingleTapUp(EditModeTextView view);

        boolean onActiveViewDoubleTap(EditModeTextView view);
    }

    public static class SimpleEventListener implements EventListener {

        @Override
        public void onViewAttached(View view) {
        }

        @Override
        public void onViewDetached(View view) {
        }

        @Override
        public boolean onActiveViewSingleTapUp(EditModeTextView view) {
            return false;
        }

        @Override
        public boolean onActiveViewDoubleTap(EditModeTextView view) {
            return false;
        }
    }

    private interface IListenerNotifier {

        void notify(EventListener listener);
    }

    private EditModeView mEditModeView;
    private EventListener mEventListener;

    private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return onAttachViewTouchEvent(v, event);
        }
    };

    private final EditModeView.EventListener mEditEventListener
            = new EditModeView.EventListener() {

        @Override
        public void onAttachViewDelete(View attachView) {
            onAttachViewDeleteEvent(attachView);
        }

        @Override
        public void onAttachViewScale(View attachView, float scale) {
            onAttachViewScaleEvent(attachView, scale);
        }

        @Override
        public boolean onAttachViewSingleTapUp(View attachView) {
            return onAttachViewSingleTapUpEvent(attachView);
        }

        @Override
        public boolean onAttachViewDoubleTap(View attachView) {
            return onAttachViewDoubleTapEvent(attachView);
        }
    };

    public TemplateLayout(Context context) {
        super(context);
        init();
    }

    public TemplateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TemplateLayout(Context context, AttributeSet attrs,
                          int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TemplateLayout(Context context, AttributeSet attrs,
                          int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setupEditModeView();
    }

    private void setupEditModeView() {
        mEditModeView = new EditModeView(getContext());
        mEditModeView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, GravityCompat.START | Gravity.TOP));
        mEditModeView.setClickable(true);
        mEditModeView.setEditListener(mEditEventListener);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            detachFromEditMode();
        }
        return super.onTouchEvent(event);
    }

    public void setActiveViewDeletable(boolean deletable) {
        mEditModeView.setDeletable(deletable);
    }

    @Nullable
    public EditModeTextView getActiveTextView() {
        final View view = mEditModeView.getAttachView();
        return (view instanceof EditModeTextView) ? (EditModeTextView) view : null;
    }

    public void setEventListener(EventListener listener) {
        mEventListener = listener;
    }

    public void addNewTextView(@NonNull EditModeTextView view) {
        addEditableView(view);
    }

    public void detachActiveView() {
        View view = mEditModeView.getAttachView();
        if (view != null) {
            detachFromEditMode();
        }
    }

    private void addEditableView(View view) {
        view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        view.setOnTouchListener(mOnTouchListener);
        addView(view);
        attachToEditMode(view);
    }

    private void attachToEditMode(View view) {
        final View oldAttachView = mEditModeView.getAttachView();
        mEditModeView.attachToView(view);
        if (oldAttachView != null) {
            notifyViewDetached(oldAttachView);
        }
        notifyViewAttached(view);
    }

    private void detachFromEditMode() {
        final View view = mEditModeView.getAttachView();
        mEditModeView.detachFromView();
        if (view != null) {
            notifyViewDetached(view);
        }
    }

    private boolean onAttachViewTouchEvent(View v, MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN && getActiveTextView() != v) {
            attachToEditMode(v);
            return true;
        }
        return false;
    }

    private void onAttachViewDeleteEvent(View attachView) {
        mEditModeView.detachFromView();
        removeView(attachView);
    }

    private void onAttachViewScaleEvent(View attachView, float scale) {
        if (attachView instanceof EditModeTextView) {
            ((EditModeTextView) attachView).onViewScale(scale);
        }
    }

    private boolean onAttachViewSingleTapUpEvent(View attachView) {
        return mEventListener != null && (attachView instanceof EditModeTextView)
                && mEventListener.onActiveViewSingleTapUp((EditModeTextView) attachView);
    }

    private boolean onAttachViewDoubleTapEvent(View attachView) {
        return mEventListener != null && (attachView instanceof EditModeTextView)
                && mEventListener.onActiveViewDoubleTap((EditModeTextView) attachView);
    }

    private void notifyListener(IListenerNotifier notifier) {
        if (mEventListener != null) {
            notifier.notify(mEventListener);
        }
    }

    private void notifyViewDetached(final View view) {
        notifyListener(new IListenerNotifier() {
            @Override
            public void notify(EventListener listener) {
                listener.onViewDetached(view);
            }
        });
    }

    private void notifyViewAttached(final View view) {
        notifyListener(new IListenerNotifier() {
            @Override
            public void notify(EventListener listener) {
                listener.onViewAttached(view);
            }
        });
    }
}
