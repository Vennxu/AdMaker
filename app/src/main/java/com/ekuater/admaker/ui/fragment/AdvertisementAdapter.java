package com.ekuater.admaker.ui.fragment;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.Scene;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.ui.widget.RoundImageView;
import com.ekuater.admaker.util.BmpUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2015/6/1.
 *
 * @author Xu wenxiang
 */
public class AdvertisementAdapter extends BaseAdapter implements View.OnClickListener {

    private Context context;
    private DisplayMetrics displayMetrics;
    private List<Scene> sceneList;
    private LayoutInflater inflater;
    private AdElementDisplay adElementDisplay;
    private SelectListener listener;
    private Scene selectedScene;

    public AdvertisementAdapter(Context context, DisplayMetrics displayMetrics, SelectListener listener) {
        this.context = context;
        this.displayMetrics = displayMetrics;
        this.listener = listener;
        sceneList = new ArrayList<>();
        inflater = LayoutInflater.from(context);
        adElementDisplay = AdElementDisplay.getInstance(context);
    }

    public void addScenes(Scene[] scenes) {
        if (scenes != null && scenes.length > 0) {
            Collections.addAll(sceneList, scenes);
        }

        if (sceneList.size() > 0 && selectedScene == null) {
            selectedScene = sceneList.get(0);
        }
    }

    @Override
    public int getCount() {
        return sceneList.size();
    }

    @Override
    public Scene getItem(int position) {
        return sceneList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.choose_item, parent, false);
            holder.cardView = (CardView) convertView.findViewById(R.id.choose_cardview);
            holder.imageView = (ImageView) convertView.findViewById(R.id.choose_item_image);
            holder.imageBg = (ImageView) convertView.findViewById(R.id.choose_item_bg);
            holder.frameLayout = (FrameLayout) convertView.findViewById(R.id.choose_fram);
            int width = (displayMetrics.widthPixels - (BmpUtils.dp2px(context, 20))) / 3;
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width,
                    (int) (width / 1.5));
            holder.frameLayout.setLayoutParams(layoutParams);
            convertView.setTag(holder);
            convertView.setOnClickListener(this);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.scene = getItem(position);
        updateSelectState(holder);
        adElementDisplay.displaySceneThumb(holder.scene, holder.imageView);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        ViewHolder holder = (ViewHolder) v.getTag();
        selectedScene = holder.scene;
        listener.onSelect(selectedScene);
        notifyDataSetChanged();
    }

    private void updateSelectState(ViewHolder holder) {
        if (holder != null) {
            holder.imageBg.setBackgroundResource(holder.scene == selectedScene
                    ? R.drawable.selected : R.drawable.normal);
        }
    }

    private class ViewHolder {
        CardView cardView;
        ImageView imageView;
        ImageView imageBg;
        FrameLayout frameLayout;

        Scene scene;
    }

    public interface SelectListener {
        void onSelect(Scene scene);
    }
}
