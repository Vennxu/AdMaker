package com.ekuater.admaker.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

public class DetectTouchGestureLayout extends FrameLayout{
	private int mTouchSlop, mDownX, mDownY, mTempX, totalMoveX, viewWidth;
	private boolean isSilding;
	private onSwipeGestureListener swipeListener;
	
	public DetectTouchGestureLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		totalMoveX = 0;
	}
	
	public interface onSwipeGestureListener{
		 void onLeftSwipe();
		 void onRightSwipe();
	}
	
	public void setSwipeGestureListener(onSwipeGestureListener listener) {
		this.swipeListener = listener;
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		viewWidth = this.getWidth();
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownX = mTempX = (int) ev.getRawX();
			mDownY = (int) ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			int moveX = (int) ev.getRawX();
			if (Math.abs(moveX - mDownX) > mTouchSlop
					&& Math.abs((int) ev.getRawY() - mDownY) < mTouchSlop) {
				return true;
			}
			break;
		}

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			int moveX = (int) event.getRawX();
			int deltaX = mTempX - moveX;
			mTempX = moveX;
			if (Math.abs(moveX - mDownX) > mTouchSlop
					&& Math.abs((int) event.getRawY() - mDownY) < mTouchSlop) {
				isSilding = true;
			}

			if (Math.abs(moveX - mDownX) >= 0 && isSilding) {
				totalMoveX += deltaX;
			}
			break;
		case MotionEvent.ACTION_UP:
			isSilding = false;
			if (Math.abs(totalMoveX) >= viewWidth / 3) {
				if (totalMoveX > 0) {
					swipeListener.onLeftSwipe();
				}else {
					swipeListener.onRightSwipe();
				}
			}
			totalMoveX = 0;
			break;
		}
		return true;
	}
	
}
