package com.ekuater.admaker.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.datastruct.eventbus.AddStickerDoneEvent;
import com.ekuater.admaker.ui.UILauncher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/7/27.
 */
public class CustomAdvertiseFragment extends BaseAdvertiesFragment {

    private static final int REQUEST_CODE = 100;

    private EventBus eventBus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventBus = EventBus.getDefault();
        eventBus.register(this);
    }

    @Override
    protected AdvertiseAdapter setAdapter() {
        return new CustomAdvertiseAdapter(getActivity());
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new GridLayoutManager(getActivity(), 2, LinearLayoutManager.VERTICAL, false);
    }

    @Override
    protected void loadMoreAdRes() {

    }

    @Override
    protected void loadAdRes() {
        AdSticker[] adStickers = adStickerManager.getCustomAdStickers();
        List<Object> objects = new ArrayList<>();
        objects.add(0, R.drawable.custom);
        objects.add(1, R.drawable.advertisement);
        Collections.addAll(objects, adStickers);
        if (objects != null) {
            mAdapter.updateData(objects);
        }
        mLoadingRes = false;
        updateLoadProgress();
    }

    @Override
    public void onItemClick(Object object, int position) {
        if (object instanceof AdSticker) {
            AdSticker adSticker = (AdSticker) object;
            listener.onStickerSelected(adSticker);
        } else if (object instanceof Integer) {
            if (position == 0) {
                UILauncher.launchCropPhotoUI(CustomAdvertiseFragment.this, 1, REQUEST_CODE, false,  getString(R.string.select_custom_image));
            } else if (position == 1) {
                UILauncher.launchCustomText(CustomAdvertiseFragment.this, REQUEST_CODE);
            }
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(AddStickerDoneEvent event) {
        loadAdRes();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        eventBus.unregister(this);
    }

    public class CustomAdvertiseAdapter extends AdvertiseAdapter {

        public CustomAdvertiseAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayout() {
            return R.layout.advertise_custom_item;
        }

        @Override
        protected AdvertiseViewHolder initViews(ViewGroup parent, int viewType) {
            return new CustomAdvertiseViewHolder(context, inflater.inflate(getLayout(), parent, false), itemClickListener);
        }

        public class CustomAdvertiseViewHolder extends AdvertiseAdapter.AdvertiseViewHolder {

            private ImageView customImage;

            public CustomAdvertiseViewHolder(Context context, View itemView, AdvertiseAdapter.ItemClickListener itemClickListener) {
                super(context, itemView, itemClickListener);
                customImage = (ImageView) itemView.findViewById(R.id.advertise_custom_image);
                customImage.setOnClickListener(this);
            }

            @Override
            protected void recender(Object object) {
                if (object instanceof AdSticker) {
                    AdSticker adSticker = (AdSticker) object;
                    adElementDisplay.displayStickerThumb(adSticker, customImage);
                } else if (object instanceof Integer) {
                    customImage.setImageResource((Integer) object);
                }
                setObject(object);
            }

            @Override
            public void onClick(View v) {
                itemClickListener.onItemClick(object, getAdapterPosition());
            }
        }
    }
}
