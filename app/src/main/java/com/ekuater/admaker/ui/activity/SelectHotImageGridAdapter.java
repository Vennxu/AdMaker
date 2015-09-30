package com.ekuater.admaker.ui.activity;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.HotIssue;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.ui.util.ScreenUtils;
import com.ekuater.admaker.util.BmpUtils;


public class SelectHotImageGridAdapter extends BaseAdapter {
	private HotIssue[] hotIssues;
	private Context mContext;
	private AdElementDisplay mDisplay;
	public SelectHotImageGridAdapter(Context context,
							HotIssue[] hotIssues) {
		this.mContext = context;
		this.hotIssues = hotIssues;
		mDisplay = AdElementDisplay.getInstance(mContext);
	}

	@Override
	public int getCount() {
		return hotIssues == null ? 0:hotIssues.length;
	}

	@Override
	public HotIssue getItem(int position) {
		return hotIssues[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_select_hot_child_image, null);
			viewHolder.cardView = (CardView) convertView
					.findViewById(R.id.hot_child_relayout);
			viewHolder.hotImage = (ImageView) convertView
					.findViewById(R.id.hot_child_image);
			int width = (ScreenUtils.getScreenWidth(mContext) -BmpUtils.dp2px(mContext, 20))/3;
			float scale = (float) 2/3;
			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (width * scale));
			viewHolder.cardView.setLayoutParams(layoutParams);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final HotIssue hotIssue = hotIssues[position];
		mDisplay.displayOnlineImage(hotIssue.getImageThumb(), viewHolder.hotImage);
		return convertView;
	}

	private class ViewHolder {
		private CardView cardView;
		private ImageView hotImage;
	}


}
