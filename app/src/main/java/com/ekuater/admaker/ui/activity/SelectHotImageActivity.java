package com.ekuater.admaker.ui.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.DayHotIssues;
import com.ekuater.admaker.datastruct.HotIssue;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.AdResLoadListener;
import com.ekuater.admaker.delegate.AdResLoader;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * Created by Administrator on 2015/8/11.
 */
public class SelectHotImageActivity extends BackIconActivity implements Handler.Callback, View.OnClickListener {

    private static final int LOAD_SUCCESS = 101;
    private static final int LOAD_FAILED = 102;
    private static final int LOAD_IMAGE_SUCCESS = 103;
    private static final int LOAD_IMAGE_FAILED = 104;

    private Handler handler = new Handler(this);

    private ImageView mHotImage;
    private ExpandableListView mList;
    private FrameLayout mFrameLayout;
    private LinearLayout mSelectHotArea;

    private AdResLoader mAdResLoader;
    private AdElementDisplay mAdElementDisplay;
    private Context mContext;
    private SelectHotImageAdapter mAdapter;
    private ProgressWheel mImageProgress;
    private ProgressWheel mListProgress;

    private HotIssue mHotIssue;

    private SelectHotImageAdapter.SelectChangeListener selectChangeListener = new SelectHotImageAdapter.SelectChangeListener() {
        @Override
        public void onSelectChange(HotIssue hotIssue) {
            mHotIssue = hotIssue;
            showHotImage(hotIssue.getImage());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_hot_image);
//        getSwipeBackLayout().setEnableGesture(false);
        mAdResLoader = AdResLoader.getInstance(this);
        mAdElementDisplay = AdElementDisplay.getInstance(this);
        mContext = this;
        initSelectHot();
        loaderData();
    }

    private void initSelectHotTitle() {
        TextView title = (TextView) findViewById(R.id.title);
        ImageView icon = (ImageView) findViewById(R.id.icon);
        TextView rightTitle = (TextView) findViewById(R.id.right_title);
        title.setText(getString(R.string.select_hot_image));
        rightTitle.setVisibility(View.VISIBLE);
        icon.setOnClickListener(this);
        rightTitle.setOnClickListener(this);
    }

    private void initSelectHot() {
        initSelectHotTitle();
        mHotImage = (ImageView) findViewById(R.id.choose_image);
        mList = (ExpandableListView) findViewById(R.id.select_hot_list);
        mImageProgress = (ProgressWheel) findViewById(R.id.choose_progress);
        mListProgress = (ProgressWheel) findViewById(R.id.select_hot_progress);
        mFrameLayout = (FrameLayout) findViewById(R.id.choose_image_frame);
        mSelectHotArea = (LinearLayout) findViewById(R.id.select_hot_area);
        mFrameLayout.setVisibility(View.GONE);
        mImageProgress.setVisibility(View.VISIBLE);
        mHotImage.setOnClickListener(this);
        mList.setGroupIndicator(null);
        mList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return true;
            }
        });
    }

    private void loaderData() {
        mAdResLoader.loadLatestDaysHotIssues(7, 3, 1, new AdResLoadListener<DayHotIssues>() {
            @Override
            public void onLoaded(boolean success, boolean remaining, DayHotIssues[] resArray) {
                handler.obtainMessage(success ? LOAD_SUCCESS : LOAD_FAILED, resArray).sendToTarget();
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handle = true;
        switch (msg.what) {
            case LOAD_SUCCESS:
                onHandlerLoadResult(msg);
                break;
            case LOAD_FAILED:
//                mListProgress.setVisibility(View.GONE);
//                mSelectHotArea.setVisibility(View.VISIBLE);
                loaderData();
                onHandlerLoadFailedResult();
                break;
            case LOAD_IMAGE_SUCCESS:
                onHandlerLoadBitmapResult(msg);
                break;
            case LOAD_IMAGE_FAILED:
                mFrameLayout.setVisibility(View.VISIBLE);
                mImageProgress.setVisibility(View.GONE);
                mHotImage.setImageResource(R.drawable.loading_picture);
                break;
            default:
                handle = false;
                break;
        }
        return handle;
    }

    private void onHandlerLoadResult(Message msg) {
        mListProgress.setVisibility(View.GONE);
        mSelectHotArea.setVisibility(View.VISIBLE);
        DayHotIssues[] dayHotIssues = (DayHotIssues[]) msg.obj;
        if (dayHotIssues != null && dayHotIssues.length > 0) {
            mAdapter = new SelectHotImageAdapter(this, dayHotIssues);
            mAdapter.setSelectChangeListener(selectChangeListener);
            mList.setAdapter(mAdapter);
            int groupCount = mList.getCount();
            for (int i = 0; i < groupCount; i++) {
                mList.expandGroup(i);
            }
            HotIssue[] hotIssue = dayHotIssues[0].getHotIssues();
            if (hotIssue != null && hotIssue.length > 0) {
                mHotIssue = hotIssue[0];
                showHotImage(hotIssue[0].getImage());
            }
        }
    }

    private void showHotImage(String url) {
        mFrameLayout.setVisibility(View.GONE);
        mImageProgress.setVisibility(View.VISIBLE);
        mAdElementDisplay.loadOnlineImage(url, new AdElementDisplay.BitmapLoadListener() {
            @Override
            public void onLoaded(Object object, boolean success, Bitmap[] bitmaps) {
                handler.obtainMessage(success ? LOAD_IMAGE_SUCCESS : LOAD_IMAGE_FAILED, bitmaps).sendToTarget();
            }
        });
    }

    private void onHandlerLoadBitmapResult(Message msg) {
        mFrameLayout.setVisibility(View.VISIBLE);
        mImageProgress.setVisibility(View.GONE);
        Bitmap[] bitmaps = (Bitmap[]) msg.obj;
        if (bitmaps != null) {
            mHotImage.setImageBitmap(bitmaps[0]);
        }
    }

    private void onHandlerLoadFailedResult() {
        if (mContext == null) {
            return;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.icon:
                finish();
                break;
            case R.id.right_title:
                launchCustomTextHotImage();
                break;
            case R.id.choose_image:
                launchCustomTextHotImage();
                break;
            default:
                break;
        }
    }

    public void launchCustomTextHotImage() {
        if (mHotIssue != null) {
            UILauncher.launchCustomTextHotImageUI(this, mHotIssue);
        }
    }
}
