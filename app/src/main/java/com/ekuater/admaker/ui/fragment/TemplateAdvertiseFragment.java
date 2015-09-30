package com.ekuater.admaker.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.AdCategoryItemVO;
import com.ekuater.admaker.datastruct.AdCategoryVO;
import com.ekuater.admaker.delegate.AdResLoadListener;
import com.ekuater.admaker.ui.util.ShowToast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2015/6/1.
 *
 * @author FanChong
 */
public class TemplateAdvertiseFragment extends BaseAdvertiesFragment {

    private static final String TAG = TemplateAdvertiseFragment.class.getSimpleName();
    private static final String AD_CATEGORISE = "ad_categorise";

    private static final int MSG_LOAD_AD_RES_RESULT = 101;
    private static final int MSG_LOAD_MORE_AD_RES_RESULT = 102;

    private LinearLayoutManager mLayoutManager;

    private int lastPosition;

    public static TemplateAdvertiseFragment newInstance(AdCategoryVO adCategoryVO) {
        TemplateAdvertiseFragment instance = new TemplateAdvertiseFragment();
        Bundle args = new Bundle();
        args.putParcelable(AD_CATEGORISE, adCategoryVO);
        instance.setArguments(args);
        return instance;
    }

    private AdCategoryVO adCategoryVO;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_LOAD_AD_RES_RESULT:
                    handleAdResResult(msg.arg1 != 0, msg.arg2 != 0, (AdCategoryItemVO[]) msg.obj);
                    break;
                case MSG_LOAD_MORE_AD_RES_RESULT:
                    handleMoreAdResResult(msg.arg1 != 0, msg.arg2 != 0, (AdCategoryItemVO[]) msg.obj);
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            adCategoryVO = bundle.getParcelable(AD_CATEGORISE);
        }
    }

    @Override
    public void setScrollListener(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        lastPosition + 1 == mAdapter.getItemCount() && remaining) {
                    loadMoreAdRes();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastPosition = mLayoutManager.findLastVisibleItemPosition();
            }
        });
    }

    @Override
    protected void loadAdRes() {
        mLoadingRes = true;
        updateLoadProgress();
        mAdResLoader.loadCategoryItems(adCategoryVO.getCategoryId(), page, new AdResLoadListener<AdCategoryItemVO>() {
            @Override
            public void onLoaded(boolean success, boolean remaining, AdCategoryItemVO[] resArray) {
                int result = success ? 1 : 0;
                int remain = remaining ? 1 : 0;
                Message message = mHandler.obtainMessage(MSG_LOAD_AD_RES_RESULT, result, remain, resArray);
                mHandler.sendMessage(message);
            }
        });
    }

    @Override
    protected void loadMoreAdRes() {
        mLoadingRes = true;
        updateLoadProgress();
        if (adCategoryVO == null) {
            return;
        }
        mAdResLoader.loadCategoryItems(adCategoryVO.getCategoryId(), page, new AdResLoadListener<AdCategoryItemVO>() {
            @Override
            public void onLoaded(boolean success, boolean remaining, AdCategoryItemVO[] resArray) {
                int result = success ? 1 : 0;
                int remain = remaining ? 1 : 0;
                Message message = mHandler.obtainMessage(MSG_LOAD_AD_RES_RESULT, result, remain, resArray);
                mHandler.sendMessage(message);
            }
        });
    }

    private void handleAdResResult(boolean success, boolean remain, AdCategoryItemVO[] adStickers) {
        mLoadingRes = false;
        updateLoadProgress();
        if (success) {
            remaining = remain;
            page += remaining ? 1 : 0;
        } else {
            if (getActivity() != null)
                ShowToast.makeText(getActivity(), R.drawable.emoji_cry, getString(R.string.load_failed)).show();
        }
        mAdapter.updateData(addStickers(adStickers));
    }

    private void handleMoreAdResResult(boolean success, boolean remain, AdCategoryItemVO[] adStickers) {
        mLoadingRes = false;
        updateLoadProgress();
        if (success) {
            remaining = remain;
            page += remaining ? 1 : 0;
        } else {
            if (getActivity() != null)
                ShowToast.makeText(getActivity(), R.drawable.emoji_cry, getString(R.string.load_failed)).show();
        }
        mAdapter.addData(addStickers(adStickers));
    }

    private List<Object> addStickers(AdCategoryItemVO[] stickers) {
        List<Object> list = new ArrayList<>();
        if (stickers != null && stickers.length > 0) {
            Collections.addAll(list, stickers);
        }
        return list;
    }


    @Override
    protected AdvertiseAdapter setAdapter() {
        return new TemplateAdapter(getActivity());
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        return mLayoutManager;
    }

    @Override
    public void onItemImageClick(Object object, int position) {
        AdCategoryItemVO adCategoryItemVO = (AdCategoryItemVO) object;
        if (adCategoryItemVO != null) {
            listener.onStickerSelected((adCategoryItemVO.getTrademark()));
        }
    }

    @Override
    public void onItemTextClick(Object object, int position) {
        AdCategoryItemVO adCategoryItemVO = (AdCategoryItemVO) object;
        if (adCategoryItemVO != null) {
            listener.onStickerSelected((adCategoryItemVO.getSlogan()));
        }
    }

    private static class TemplateAdapter extends AdvertiseAdapter {

        public TemplateAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayout() {
            return R.layout.advertise_template_item;
        }

        @Override
        protected AdvertiseViewHolder initViews(ViewGroup parent, int viewType) {
            return new TemplateViewHolder(context, inflater.inflate(getLayout(), parent, false), itemClickListener);
        }

        public static class TemplateViewHolder extends AdvertiseAdapter.AdvertiseViewHolder implements View.OnClickListener {
            private ImageView advertise;
            private ImageView advertise_text;

            public TemplateViewHolder(Context context, View itemView, ItemClickListener itemClickListener) {
                super(context, itemView, itemClickListener);
                WindowManager wm = (WindowManager) context
                        .getSystemService(Context.WINDOW_SERVICE);
                int width = wm.getDefaultDisplay().getWidth();
                advertise = (ImageView) itemView.findViewById(R.id.advertise_image);
                advertise_text = (ImageView) itemView.findViewById(R.id.advertise_text);
                advertise.setOnClickListener(this);
                advertise_text.setOnClickListener(this);
            }

            @Override
            protected void recender(Object object) {
                AdCategoryItemVO adCategoryItemVO = (AdCategoryItemVO) object;
                if (adCategoryItemVO != null) {
                    advertise.setImageResource(R.drawable.loading_picture);
                    adElementDisplay.displayStickerThumb(adCategoryItemVO.getTrademark(), advertise);
                    adElementDisplay.displayStickerThumb(adCategoryItemVO.getSlogan(), advertise_text);
                    itemView.setOnClickListener(this);
                    setObject(adCategoryItemVO);
                }
            }

            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    switch (v.getId()) {
                        case R.id.advertise_text:
                            itemClickListener.onItemTextClick(object, getAdapterPosition());
                            break;
                        case R.id.advertise_image:
                            itemClickListener.onItemImageClick(object, getAdapterPosition());
                            break;
                        default:
                            break;
                    }
                }
            }

        }
    }


}
