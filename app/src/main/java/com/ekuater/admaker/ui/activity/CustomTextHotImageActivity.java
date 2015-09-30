package com.ekuater.admaker.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ekuater.admaker.EnvConfig;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.datastruct.HotIssue;
import com.ekuater.admaker.datastruct.Term;
import com.ekuater.admaker.datastruct.eventbus.AddStickerDoneEvent;
import com.ekuater.admaker.datastruct.eventbus.ColorEvent;
import com.ekuater.admaker.datastruct.eventbus.CustomTextEvent;
import com.ekuater.admaker.datastruct.eventbus.FontEvent;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.AdStickerManager;
import com.ekuater.admaker.delegate.CustomTextManager;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.fragment.text.ColorTextFragment;
import com.ekuater.admaker.ui.fragment.text.EffectListener;
import com.ekuater.admaker.ui.fragment.text.EffectTextFragment;
import com.ekuater.admaker.ui.fragment.text.FontTextFragment;
import com.ekuater.admaker.ui.widget.EditModeTextView;
import com.ekuater.admaker.ui.widget.PagerSlidingTabStrip;
import com.ekuater.admaker.ui.widget.TemplateLayout;
import com.ekuater.admaker.ui.widget.VerticalTextView;
import com.ekuater.admaker.util.BmpUtils;
import com.ekuater.admaker.util.TextUtil;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/8/18.
 *
 * @author Xu Wenxiang
 */
public class CustomTextHotImageActivity extends BackIconActivity implements View.OnClickListener, Handler.Callback {
    public static final String CUSTOM_TEXT = "custom_text";
    public static final String HOTISSUE = "hotissue";
    public static final String HOTIMAGE_URL = "hot_image_url.png";
    private static final int LOAD_BITMAP_SUCCESS = 101;
    private static final int LOAD_BITMAP_FAILED = 102;
    private TemplateLayout templateLayout;
    private ViewPager viewPager;
    private PagerSlidingTabStrip pagerSlidingTabStrip;
    private RelativeLayout linearLayout;
    private TextView rightTitle;
    private ProgressWheel progressWheel;
    private ImageView mImageView;

    private ArrayList<Fragment> list;

    private CustomTextManager mCustomTextManager;
    private AdElementDisplay mAdElementDisplay;
    private CustomTextFragmentAdapter mAdapter;
    private EventBus mUIEventBus;
    private AdStickerManager mAdStickerManager;
    private String mCustomText = null;
    private EditModeTextView textVertical;
    private HotIssue hotIssue;
    private Handler mHandler = new Handler(this);
    private Bitmap mBitmap;

    private EffectListener effectListener = new EffectListener() {

        @Override
        public void onRadioNormal() {
            textVertical.setEffectNormal();
        }

        @Override
        public void onRadioLuminess() {
            textVertical.setEffectShadow(5, 0, 0);
        }

        @Override
        public void onRadioStroke() {
            textVertical.setEffectStroke(2);
        }

        @Override
        public void onRadioVertical() {
            textVertical.setOrientation(VerticalTextView.VERTICAL);
        }

        @Override
        public void onRadioHorizontal() {
            textVertical.setOrientation(VerticalTextView.HORIZONTAL);
        }

        @Override
        public void onRadioCenterAlign() {
            textVertical.setAlignment(VerticalTextView.ALIGN_CENTER);
        }

        @Override
        public void onRadioLeftAlign() {
            textVertical.setAlignment(VerticalTextView.ALIGN_LEFT);
        }

        @Override
        public void onRadioRightAlign() {
            textVertical.setAlignment(VerticalTextView.ALIGN_RIGHT);
        }

        @Override
        public void onRadioWhiteColor() {
            textVertical.setEffectColor(getResources().getColor(R.color.f2_stroke));
        }

        @Override
        public void onRadioBlackColor() {
            textVertical.setEffectColor(Color.BLACK);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSwipeBackLayout().setEnableGesture(false);
        UILauncher.launchInputCustomText(CustomTextHotImageActivity.this, mCustomText);
        setContentView(R.layout.activity_custom_text_hot_image);
        mAdElementDisplay = AdElementDisplay.getInstance(this);
        mCustomTextManager = CustomTextManager.getInstance(this);
        mAdStickerManager = AdStickerManager.getInstance(this);
        mUIEventBus = UIEventBusHub.getDefaultEventBus();
        mUIEventBus.register(this);
        getParamArguments();
        initView();
    }

    private void getParamArguments(){
        Intent intent = getIntent();
        if (intent != null){
            hotIssue = intent.getParcelableExtra(HOTISSUE);
        }
    }

    private void initView() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(getString(R.string.speak_hot_image));
        ImageView icon = (ImageView) findViewById(R.id.icon);
        icon.setOnClickListener(this);
        rightTitle = (TextView) findViewById(R.id.right_title);
        rightTitle.setVisibility(View.VISIBLE);
        rightTitle.setOnClickListener(this);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.pager_tab);
        linearLayout = (RelativeLayout) findViewById(R.id.custom_area);
        templateLayout = (TemplateLayout) findViewById(R.id.custom_text_hot_image_template);
        progressWheel = (ProgressWheel) findViewById(R.id.custom_text_hot_image_progress);
        mImageView = (ImageView) findViewById(R.id.custom_text_hot_image);
        list = new ArrayList<>();
        getTerms(Term.FONT);
        getTerms(Term.COLOR);
        getTerms(Term.EFFECT);
        mAdapter = new CustomTextFragmentAdapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(mAdapter);
        pagerSlidingTabStrip.setViewPager(viewPager);
        addEditModeTextView();
        measureLayout();
        loadImageBitmap();
        templateLayout.setActiveViewDeletable(false);
        templateLayout.setEventListener(new TemplateLayout.SimpleEventListener() {
            @Override
            public boolean onActiveViewSingleTapUp(EditModeTextView view) {
                textVertical = templateLayout.getActiveTextView();
                UILauncher.launchInputCustomText(CustomTextHotImageActivity.this, mCustomText);
                return true;
            }
        });
    }

    private void measureLayout(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = (int) ((metrics.heightPixels - (BmpUtils.dp2px(this, 50)))*0.4);
        ViewGroup.LayoutParams layoutParams= linearLayout.getLayoutParams();
        layoutParams.height = height;
        linearLayout.setLayoutParams(layoutParams);
    }

    private void addEditModeTextView(){
        EditModeTextView textView = new EditModeTextView(this);
        textView.setText(getString(R.string.click_edit));
        textView.setTextColor(getResources().getColor(R.color.font_color_normal));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        File file = new File(EnvConfig.genFontFile().getAbsolutePath() + "/" + "fzzdh.ttf");
        textView.setTypeface(Typeface.createFromFile(file));
        textView.setEffectColor(getResources().getColor(R.color.f2_stroke));
        textView.setEffectStroke(2);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textVertical = textView;
        templateLayout.addNewTextView(textView);
    }

    private void loadImageBitmap(){
        mAdElementDisplay.loadOnlineImage(hotIssue.getImage(), new AdElementDisplay.BitmapLoadListener() {
            @Override
            public void onLoaded(Object object, boolean success, Bitmap[] bitmaps) {
                mHandler.obtainMessage(success ? LOAD_BITMAP_SUCCESS : LOAD_BITMAP_FAILED, bitmaps).sendToTarget();
            }
        });
    }

    private void getTerms(int type) {
        switch (type) {
            case Term.FONT:
                ArrayList<Term> fonts = mCustomTextManager.getTermFont(getResources());
                list.add(FontTextFragment.newInstance(fonts, Term.FONT, getString(R.string.font_example_hot_text)));
                mCustomTextManager.copyToSD(this, fonts);
                break;
            case Term.COLOR:
                list.add(new ColorTextFragment());
                break;
            case Term.EFFECT:
                list.add(EffectTextFragment.newInstance(effectListener));
                break;
            default:
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.icon:
                finish();
                break;
            case R.id.right_title:
                if (mBitmap != null) {
                    templateLayout.detachActiveView();
                    Bitmap bgBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bgBitmap);
                    Bitmap imageBitmap = Bitmap.createBitmap(mImageView.getWidth(), mImageView.getHeight(), Bitmap.Config.ARGB_8888);
                    Bitmap templateBitmap = Bitmap.createBitmap(templateLayout.getWidth(), templateLayout.getHeight(), Bitmap.Config.ARGB_8888);
                    mImageView.draw(new Canvas(imageBitmap));
                    templateLayout.draw(new Canvas(templateBitmap));
                    Matrix matrix = new Matrix();
                    matrix.setScale((float) canvas.getWidth() / imageBitmap.getWidth(), (float) canvas.getHeight() / imageBitmap.getHeight(), (float) canvas.getWidth() / 2, (float) canvas.getHeight() / 2);
                    matrix.preTranslate((canvas.getWidth() - imageBitmap.getWidth()) / 2, (canvas.getHeight() - imageBitmap.getHeight()) / 2);
                    canvas.drawBitmap(imageBitmap, matrix, new Paint());
                    if (!TextUtil.isEmpty(mCustomText)) {
                        canvas.drawBitmap(templateBitmap, matrix, new Paint());
                    }
                    File tmpFile = EnvConfig.genHotImageFile(HOTIMAGE_URL);
                    BmpUtils.saveBitmapToFile(bgBitmap, tmpFile);
                    UILauncher.launchPushHotIsssueUI(this);
                }
                break;
        }
    }

    private class CustomTextFragmentAdapter extends FragmentPagerAdapter {
        private List<Fragment> mList;
        private String[] mTitles = new String[]{getString(R.string.font), getString(R.string.color), getString(R.string.effect)};

        public CustomTextFragmentAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            mList = list;
        }

        @Override
        public Fragment getItem(int position) {
            return mList.get(position);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public String getPageTitle(int position) {
            return mTitles[position];
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(CustomTextEvent event) {
        String content = event.getContent();
        if (!TextUtil.isEmpty(content)) {
            mCustomText = content;
            textVertical.setText(content);
        } else {
            mCustomText = null;
            textVertical.setText(getString(R.string.click_edit));
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(ColorEvent event) {
        int color = event.getColor();
        if (color != 0) {
            textVertical.setTextColor(color);
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(FontEvent event) {
        String font = event.getFont();
        Typeface customFont = null;
        if (!TextUtil.isEmpty(font)) {
            File file = new File(EnvConfig.genFontFile().getAbsolutePath() + "/" + font);
            customFont = Typeface.createFromFile(file);
        }
        textVertical.setTypeface(customFont);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUIEventBus.isRegistered(this)) {
            mUIEventBus.unregister(this);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handle = true;
        switch (msg.what){
            case LOAD_BITMAP_SUCCESS:
                progressWheel.setVisibility(View.GONE);
                Bitmap[] bitmaps = (Bitmap[]) msg.obj;
                if (bitmaps != null && bitmaps.length > 0){
                    mBitmap = bitmaps[0];
                    mImageView.setImageBitmap(bitmaps[0]);
                }
                break;
            case LOAD_BITMAP_FAILED:
                progressWheel.setVisibility(View.GONE);
                break;
            default:
                handle = false;
                break;
        }
        return handle;
    }
}
