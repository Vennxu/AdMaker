package com.ekuater.admaker.ui.fragment.text;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.Term;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2015/6/1.
 *
 * @author Xu wenxiang
 */
public abstract class CustomTextFragment extends Fragment {

    private static final String TAG = CustomTextFragment.class.getSimpleName();
    public static final String ARGS_AD_TERMEFFECT = "ARGS_AD_TERMEFFECT";
    public static final String ARGS_AD_TERMEFFECT_TYPE = "type";
    public static final String ARGS_AD_ROW = "row";
    public static final String ARGS_AD_COLUMS = "column";

    public int row;
    public int column;
    public int mType;
    public ArrayList<Term> terms;
    public List<ImageView> imageViewList;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        terms = (args != null)
                ? args.<Term>getParcelableArrayList(ARGS_AD_TERMEFFECT) : null;
        mType = args.getInt(ARGS_AD_TERMEFFECT_TYPE);
        row = args.getInt(ARGS_AD_ROW);
        column = args.getInt(ARGS_AD_COLUMS);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_custom_text, container, false);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(onPageChangeListener);

        ViewPagerAdapter adapter = new ViewPagerAdapter(initDate(inflater, viewPager));
        viewPager.setAdapter(adapter);
        return view;
    }


    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < imageViewList.size(); i++) {
                if (i == position) {
                    imageViewList.get(i).setImageResource(R.drawable.selected_state);
                } else {
                    imageViewList.get(i).setImageResource(R.drawable.normal_state);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    protected abstract List<RecyclerView> initDate(LayoutInflater inflater, ViewGroup container);


    public class ViewPagerAdapter extends PagerAdapter {
        private List<RecyclerView> mList;

        public ViewPagerAdapter(List<RecyclerView> list) {
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = mList.get(position);
            container.addView(v);
            return v;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public interface ChangeSelectListener {
        void OnChangeSelect(TextView view, int positon);
    }
}
