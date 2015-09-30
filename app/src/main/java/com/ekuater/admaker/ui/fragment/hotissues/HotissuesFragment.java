package com.ekuater.admaker.ui.fragment.hotissues;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.ui.util.BitmapUtils;
import com.ekuater.admaker.util.BmpUtils;

/**
 * Created by Administrator on 2015/8/12.
 */
public class HotissuesFragment extends Fragment {

    private static final String PORTFOLIOVO = "portfoliovo";

    private GridView mHotGrid;
    private View mRootView;
    private LayoutInflater mInflater;
    private AdElementDisplay mAdElementDisplay;
    private Activity mContext;
    private PortfolioVO[] portfolioVOs;
    private DisplayMetrics metrics;

    public static HotissuesFragment getInstance(PortfolioVO[] portfolioVOs){
        HotissuesFragment hotissuesFragment = new HotissuesFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArray(PORTFOLIOVO, portfolioVOs);
        hotissuesFragment.setArguments(bundle);
        return hotissuesFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mAdElementDisplay = AdElementDisplay.getInstance(mContext);
        metrics = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        getParamArguments();
    }

    private void getParamArguments(){
        Bundle bundle = getArguments();
        if (bundle != null){
            portfolioVOs = (PortfolioVO[]) bundle.getParcelableArray(PORTFOLIOVO);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null){
            mRootView = inflater.inflate(R.layout.fragment_hotissues, container, false);
            mInflater = inflater;
            mHotGrid = (GridView) mRootView.findViewById(R.id.hotissues_gridview);
            mHotGrid.setAdapter(new HotissuesAdapter());
        }
        return mRootView;
    }

    private class HotissuesAdapter extends BaseAdapter{

        public HotissuesAdapter(){

        }

        @Override
        public int getCount() {
            return portfolioVOs.length;
        }

        @Override
        public PortfolioVO getItem(int position) {
            return portfolioVOs[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HotissuesViewHolder holder;
            if (convertView == null){
                holder = new HotissuesViewHolder();
                convertView = mInflater.inflate(R.layout.item_select_hot_child_image, parent, false);
                holder.imageView = (ImageView) convertView.findViewById(R.id.hot_child_image);
                mAdElementDisplay.loadPortfolioImage(getItem(position), mContext, holder.imageView, (metrics.widthPixels-BmpUtils.dp2px(mContext, 30))/2);
            }
            return convertView;
        }

        private class HotissuesViewHolder{
            private ImageView imageView;
        }
    }
}
