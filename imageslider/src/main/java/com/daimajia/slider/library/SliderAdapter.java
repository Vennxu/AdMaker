package com.daimajia.slider.library;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;

import java.util.ArrayList;
import java.util.List;

/**
 * A slider adapter
 */
public class SliderAdapter extends PagerAdapter{

    private Context mContext;
    private ArrayList<BaseSliderView> mImageContents;

    public SliderAdapter(Context context){
        mContext = context;
        mImageContents = new ArrayList<>();
    }

    public <T extends BaseSliderView> void addSlider(T slider){
        mImageContents.add(slider);
        notifyDataSetChanged();
    }

    public <T extends List<BaseSliderView>> void addNewSlider(T sliderViews){
        if (mImageContents != null){
            mImageContents.clear();
        }
        mImageContents.addAll(sliderViews);
        notifyDataSetChanged();
    }

    public BaseSliderView getSliderView(int position){
        if(position < 0 || position >= mImageContents.size()){
            return null;
        }else{
            return mImageContents.get(position);
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void removeSliderAt(int position){
        if(mImageContents.size() > position){
            mImageContents.remove(position);
            notifyDataSetChanged();
        }
    }

    public void removeAllSliders(){
        mImageContents.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mImageContents.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        BaseSliderView b = mImageContents.get(position);
        View v = b.getView();
        container.addView(v);
        return v;
    }
}
