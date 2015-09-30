package com.ekuater.admaker.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.AdCategoryVO;
import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.datastruct.Scene;
import com.ekuater.admaker.datastruct.eventbus.FinishEvent;
import com.ekuater.admaker.delegate.AdResLoadListener;
import com.ekuater.admaker.delegate.AdResLoader;
import com.ekuater.admaker.delegate.AdStickerManager;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.fragment.AdStickerListener;
import com.ekuater.admaker.ui.fragment.AdWorkspaceFragment;
import com.ekuater.admaker.ui.fragment.AdWorkspaceListener;
import com.ekuater.admaker.ui.fragment.CustomAdvertiseFragment;
import com.ekuater.admaker.ui.fragment.TemplateAdvertiseFragment;
import com.ekuater.admaker.ui.fragment.SimpleProgressHelper;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.ui.widget.PagerSlidingTabStrip;
import com.ekuater.admaker.ui.widget.VerticalViewPager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/5/29.
 *
 * @author FanChong
 */
public class AdvertiseActivity extends BackIconActivity
        implements AdWorkspaceListener, AdStickerListener ,Handler.Callback{

    public static final String SCENE = "scene";
    public static final String EXTRA_OUTPUT_PATH = "output_path";
    public static final String EXTRA_OUTPUT_SCENE = "output_scene";

    public static final int RESULT_CODE = 101;

    public static final int HANDLER_LOAD_CATEGORIES_SUCCESS = 102;
    public static final int HANDLER_LOAD_CATEGORIES_FAILED = 103;

    private static final int CUSTOM = 0;
    private static final int ADVERTISE = 1;

    private TextView moreOption;
    private ListView mCategoriesList;
    private VerticalViewPager viewPager;
    private AdvertiseAdapter mCategoriesAdapter;

    private AdWorkspaceFragment mWorkspace;
    private SimpleProgressHelper mProgressHelper;
    private boolean mBaseImageReady;
    public Scene mScene;
    private String mOutputPath;
    private AdStickerManager mManager;
    private AdResLoader mAdResLoader;
    private EventBus mEventBus;

    private Handler handler = new Handler(this);

    @Override
    public boolean handleMessage(Message msg) {
        boolean handle = true;
        switch (msg.what){
            case HANDLER_LOAD_CATEGORIES_SUCCESS:
                onHandlerLoadCategories(msg);
                break;
            case HANDLER_LOAD_CATEGORIES_FAILED:
                List<Fragment> mFragmentList = new ArrayList<>();
                mFragmentList.add(new CustomAdvertiseFragment());
                AdvertiseFragmentAdapter adapter = new AdvertiseFragmentAdapter(getSupportFragmentManager(), mFragmentList);
                viewPager.setAdapter(adapter);
                break;
            default:
                handle = true;
                break;
        }
        return handle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise);
        getSwipeBackLayout().setEnableGesture(false);
        mEventBus = UIEventBusHub.getDefaultEventBus();
        mEventBus.register(this);
        mManager = AdStickerManager.getInstance(this);
        mAdResLoader = AdResLoader.getInstance(this);
        findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(getString(R.string.make_billboard));
        moreOption = (TextView) findViewById(R.id.right_title);
        moreOption.setVisibility(View.VISIBLE);
        moreOption.setText(getString(R.string.create));
        moreOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWorkspace.saveStickers();
            }
        });
        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.pager_tab);

        viewPager = (VerticalViewPager) findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(1);
        mCategoriesList = (ListView) findViewById(R.id.list_view);
        mCategoriesAdapter = new AdvertiseAdapter(this);
        mCategoriesList.setAdapter(mCategoriesAdapter);

        FragmentManager fm = getSupportFragmentManager();
        mWorkspace = (AdWorkspaceFragment) fm.findFragmentById(R.id.fragment_workspace);
//        pagerSlidingTabStrip.setViewPager(viewPager);
        mBaseImageReady = false;
        mScene = getIntent().getParcelableExtra(SCENE);
        mOutputPath = getIntent().getStringExtra(EXTRA_OUTPUT_PATH);
        mProgressHelper = new SimpleProgressHelper(this);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCategoriesAdapter.setPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mCategoriesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCategoriesAdapter.setPosition(position);
                viewPager.setCurrentItem(position);
            }
        });
        getLoadCategories();
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(FinishEvent event) {
       finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mWorkspace.onCropPhoto(RESULT_OK, new Intent().setData(getIntent().getData()));
    }

    @Override
    public void onBaseImageReady(Bitmap baseImage) {
        mBaseImageReady = (baseImage != null);
//        if (baseImage != null) {
//            moreOption.setTextColor(getResources().getColor(R.color.white));
//        }
    }

    @Override
    public void onSaveStickersDone(Bitmap savedBitmap) {
        if (savedBitmap != null && !savedBitmap.isRecycled()) {
            if (TextUtils.isEmpty(mOutputPath)) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            } else {
                new SaveImageTask().execute(savedBitmap);
            }
        }
    }

    @Override
    public void onStickerSelected(AdSticker sticker) {
        if (mBaseImageReady && sticker != null) {
            mWorkspace.addSticker(sticker);
            mManager.addRecentAdSticker(sticker);
        } else {
            ShowToast.makeText(this, R.drawable.emoji_sad,
                    getString(R.string.select_image_tip)).show();
        }
    }

    private class SaveImageTask extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... params) {
            File f = new File(mOutputPath);
            if (f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
            try {
                FileOutputStream out = new FileOutputStream(f);
                params[0].compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mProgressHelper.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressHelper.dismiss();
            UILauncher.launchOperationAdvertiseUI(AdvertiseActivity.this,
                    mScene, mOutputPath);
        }
    }
    private int mPage = 1;
    public void getLoadCategories(){
        mAdResLoader.loadCategories(mPage, new AdResLoadListener<AdCategoryVO>() {
            @Override
            public void onLoaded(boolean success, boolean remaining, AdCategoryVO[] resArray) {
                handler.obtainMessage(success ? HANDLER_LOAD_CATEGORIES_SUCCESS : HANDLER_LOAD_CATEGORIES_FAILED, resArray).sendToTarget();
            }
        });
    }

    private void onHandlerLoadCategories(Message msg){
        AdCategoryVO[] categoryVOs = (AdCategoryVO[]) msg.obj;
        if (categoryVOs != null && categoryVOs.length > 0){
            mCategoriesAdapter.addNewsData(categoryVOs);
            List<Fragment> mFragmentList = new ArrayList<>();
            mFragmentList.add(new CustomAdvertiseFragment());
            for (int i = 0; i < categoryVOs.length; i++) {
                mFragmentList.add(TemplateAdvertiseFragment.newInstance(categoryVOs[i]));
            }
            AdvertiseFragmentAdapter adapter = new AdvertiseFragmentAdapter(getSupportFragmentManager(), mFragmentList);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(1);
        }
    }

    private class AdvertiseFragmentAdapter extends FragmentPagerAdapter {
        private List<Fragment> mList;
        private List<String> mTitleList = new ArrayList<>();

        public AdvertiseFragmentAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            mList = list;
            mTitleList.add(getString(R.string.trademark));
            mTitleList.add(getString(R.string.slogan));
            mTitleList.add(getString(R.string.recently_used));

        }

        @Override
        public Fragment getItem(int position) {
            return mList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (0 <= position && position < mTitleList.size()) {
                return mTitleList.get(position);
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return mList.size();
        }
    }

    private class AdvertiseAdapter extends BaseAdapter{
        private List<Object> categorises;
        private Context context;
        private LayoutInflater inflater;
        private int mPosition = 1;

        public AdvertiseAdapter(Context context){
            this.context = context;
            categorises = new ArrayList<>();
            categorises.add(0, context.getString(R.string.custom));
            inflater = LayoutInflater.from(context);
        }

        public void addNewsData(AdCategoryVO[] categorises){
            if (categorises != null && categorises.length > 0) {
                Collections.addAll(this.categorises, categorises);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return categorises.size();
        }

        @Override
        public Object getItem(int position) {
            return categorises.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.item_advertise_left, parent, false);
            TextView textView = (TextView) view.findViewById(R.id.item_left_text);
            Object object = getItem(position);
            if (object instanceof AdCategoryVO) {
                AdCategoryVO adCategoryVO = (AdCategoryVO) getItem(position);
                textView.setText(adCategoryVO.getCategoryName());
            }else if (object instanceof String){
                textView.setText(object.toString());
            }
            if (position == 0){
                textView.setTextColor(getResources().getColor(R.color.custom_list_color));
            }
            textView.setBackgroundColor(getResources().getColor(mPosition == position ? R.color.left_advertise_selected : R.color.white));
            return view;
        }

        public void setPosition(int mPosition) {
            this.mPosition = mPosition;
            notifyDataSetChanged();
        }
    }
}
