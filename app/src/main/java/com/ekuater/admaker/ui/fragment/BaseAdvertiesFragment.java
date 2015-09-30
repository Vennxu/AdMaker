package com.ekuater.admaker.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.delegate.AdResLoader;
import com.ekuater.admaker.delegate.AdStickerManager;
import com.ekuater.admaker.ui.activity.CustomTextActivity;
import com.ekuater.admaker.ui.widget.CustomRecycler;
import com.pnikosis.materialishprogress.ProgressWheel;


/**
 * Created by Administrator on 2015/7/25.
 * @author Xu wenxiang
 */
public abstract class BaseAdvertiesFragment extends Fragment implements AdvertiseAdapter.ItemClickListener{

    private static final int REQUEST_CODE = 100;
    protected AdStickerListener listener;
    protected AdResLoader mAdResLoader;
    protected int page = 1;
    protected boolean remaining;
    protected AdStickerManager adStickerManager;
    private ProgressWheel mLoadProgress;
    protected boolean mLoadingRes = true;
    protected CustomRecycler mCustomRecycler;
    protected AdvertiseAdapter mAdapter;
    private View rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdResLoader = AdResLoader.getInstance(getActivity());
        adStickerManager = AdStickerManager.getInstance(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_advertise, container, false);
            mCustomRecycler = (CustomRecycler) rootView.findViewById(R.id.grid_view);
            mLoadProgress = (ProgressWheel) rootView.findViewById(R.id.load_progress);
            updateLoadProgress();
            mCustomRecycler.setLayoutManager(getLayoutManager());
            mAdapter = setAdapter();
            mCustomRecycler.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(this);
            setScrollListener(mCustomRecycler);
            loadAdRes();
        }
        return rootView;
    }

    protected abstract AdvertiseAdapter setAdapter();

    protected abstract RecyclerView.LayoutManager getLayoutManager();

    @Override
    public void onItemClick(Object object, int position) {

    }

    @Override
    public void onItemTextClick(Object object, int position) {

    }

    @Override
    public void onItemImageClick(Object object, int position) {

    }

    public void setScrollListener(RecyclerView recyclerView){

    }

    protected abstract void loadMoreAdRes();

    protected abstract void loadAdRes();

    protected void updateLoadProgress() {
        if (mLoadProgress != null) {
            mLoadProgress.setVisibility(mLoadingRes ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null) {
            listener.onStickerSelected(data.<AdSticker>getParcelableExtra(CustomTextActivity.CUSTOM_TEXT));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (AdStickerListener) activity;
        } catch (ClassCastException e) {
            listener = null;
        }
    }
}
