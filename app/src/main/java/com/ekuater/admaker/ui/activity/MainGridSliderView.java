package com.ekuater.admaker.ui.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.ui.util.ScreenUtils;
import com.ekuater.admaker.util.BmpUtils;

/**
 * This is a slider with a description TextView.
 */
public class MainGridSliderView extends BaseSliderView {

    private int viewPagerHeight;
    private PortfolioVO[] data;
    private MainHotImageActivity.HotissuesAdapter.SliderLayoutListener onclickTest;

    public MainGridSliderView(Context context, int viewPagerHeight, MainHotImageActivity.HotissuesAdapter.SliderLayoutListener onclickTest) {
        super(context);
        this.viewPagerHeight = viewPagerHeight;
        this.onclickTest = onclickTest;
    }

    @Override
    public View getView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_viewpager_gridview, null, false);
        GridView gridView = (GridView) view.findViewById(R.id.item_viewpager_grid);
        int width = (ScreenUtils.getScreenWidth(getContext()) - BmpUtils.dp2px(mContext, 30)) / 2;
        float scale = (float) 2 / 3;
        int height = (int) (width * scale);
        int overMargin = (viewPagerHeight - (height * 2)) / 3;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) gridView.getLayoutParams();
        layoutParams.setMargins(BmpUtils.dp2px(mContext, 10), overMargin, BmpUtils.dp2px(mContext, 10), 0);
        gridView.setVerticalSpacing(overMargin);

        final MainHotImageActivity.HotissuesAdapter hotissuesAdapter = new MainHotImageActivity.HotissuesAdapter(mContext,data ,onclickTest);
        gridView.setAdapter(hotissuesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onclickTest.onItemClick(hotissuesAdapter, data[position], position);
            }
        });
        return view;
    }

    public void setPortfolios(PortfolioVO[] portfolios){
        this.data = portfolios;
    }
}
