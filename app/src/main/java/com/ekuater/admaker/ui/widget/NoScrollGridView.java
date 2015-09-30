package com.ekuater.admaker.ui.widget;

import android.widget.GridView;

public class NoScrollGridView extends GridView {
	public NoScrollGridView(android.content.Context context,
			android.util.AttributeSet attrs)
	{
		super(context, attrs);
	}


	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);

	}
}
