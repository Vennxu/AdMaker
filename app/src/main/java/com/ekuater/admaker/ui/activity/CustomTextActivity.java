package com.ekuater.admaker.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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
import android.widget.TextView;

import com.ekuater.admaker.EnvConfig;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.datastruct.Term;
import com.ekuater.admaker.datastruct.eventbus.ColorEvent;
import com.ekuater.admaker.datastruct.eventbus.AddStickerDoneEvent;
import com.ekuater.admaker.datastruct.eventbus.CustomTextEvent;
import com.ekuater.admaker.datastruct.eventbus.FontEvent;
import com.ekuater.admaker.delegate.AdStickerManager;
import com.ekuater.admaker.delegate.CustomTextManager;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.fragment.text.ColorTextFragment;
import com.ekuater.admaker.ui.fragment.text.EffectListener;
import com.ekuater.admaker.ui.fragment.text.EffectTextFragment;
import com.ekuater.admaker.ui.fragment.text.FontTextFragment;
import com.ekuater.admaker.ui.widget.PagerSlidingTabStrip;
import com.ekuater.admaker.ui.widget.VerticalTextView;
import com.ekuater.admaker.util.BmpUtils;
import com.ekuater.admaker.util.TextUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/6/8.
 *
 * @author Xu Wenxiang
 */
public class CustomTextActivity extends BackIconActivity implements View.OnClickListener {
    public static final String CUSTOM_TEXT = "custom_text";

    private VerticalTextView textVertical;
    private ViewPager viewPager;
    private PagerSlidingTabStrip pagerSlidingTabStrip;
    private LinearLayout linearLayout;
    private TextView rightTitle;

    private ArrayList<Fragment> list;

    private CustomTextManager mCustomTextManager;
    private CustomTextFragmentAdapter mAdapter;
    private EventBus mUIEventBus;
    private AdStickerManager mAdStickerManager;
    private String mCustomText = null;

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
        UILauncher.launchInputCustomText(CustomTextActivity.this, mCustomText);
        setContentView(R.layout.activity_custom_text);
        mCustomTextManager = CustomTextManager.getInstance(this);
        mAdStickerManager = AdStickerManager.getInstance(this);
        mUIEventBus = UIEventBusHub.getDefaultEventBus();
        mUIEventBus.register(this);
        initView();
    }

    private void initView() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(getString(R.string.custom_edit));
        ImageView icon = (ImageView) findViewById(R.id.icon);
        icon.setOnClickListener(this);
        rightTitle = (TextView) findViewById(R.id.right_title);
        rightTitle.setVisibility(View.VISIBLE);
        rightTitle.setEnabled(false);
        rightTitle.setTextColor(getResources().getColor(R.color.no_enable));
        rightTitle.setOnClickListener(this);
        textVertical = (VerticalTextView) findViewById(R.id.custom_text);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.pager_tab);
        linearLayout = (LinearLayout) findViewById(R.id.custom_area);
        list = new ArrayList<>();
        getTerms(Term.FONT);
        getTerms(Term.COLOR);
        getTerms(Term.EFFECT);
        mAdapter = new CustomTextFragmentAdapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(mAdapter);
        pagerSlidingTabStrip.setViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        textVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UILauncher.launchInputCustomText(CustomTextActivity.this, mCustomText);
            }
        });

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UILauncher.launchInputCustomText(CustomTextActivity.this, mCustomText);
            }
        });
//        textVertical.setText(getString(R.string.click_edit));
        textVertical.setTextColor(getResources().getColor(R.color.font_color_normal));
        File file = new File(EnvConfig.genFontFile().getAbsolutePath() + "/" + "fzzdh.ttf");
        textVertical.setTypeface(Typeface.createFromFile(file));
        textVertical.setEffectColor(getResources().getColor(R.color.f2_stroke));
        textVertical.setEffectStroke(2);
        textVertical.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        measureLayout();
    }

    private void measureLayout(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = (int) ((metrics.heightPixels - (BmpUtils.dp2px(this, 50)))*0.4);
        ViewGroup.LayoutParams layoutParams= linearLayout.getLayoutParams();
        layoutParams.height = height;
        linearLayout.setLayoutParams(layoutParams);
    }

    private void getTerms(int type) {
        switch (type) {
            case Term.FONT:
                ArrayList<Term> fonts = mCustomTextManager.getTermFont(getResources());
                list.add(FontTextFragment.newInstance(fonts, Term.FONT, getString(R.string.font_example_text)));
                mCustomTextManager.copyToSD(this, fonts);
                break;
            case Term.COLOR:
                ArrayList<Term> colors = mCustomTextManager.getTermColor(getResources());
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
                Bitmap imageBitmap = Bitmap.createBitmap(textVertical.getWidth(), textVertical.getHeight(), Bitmap.Config.ARGB_8888);
                textVertical.draw(new Canvas(imageBitmap));
//                Bitmap thumbBitmap = mCustomTextManager.getRectBitmap(imageBitmap, getResources().getColor(R.color.transparent));
                AdSticker adSticker = mAdStickerManager.addNewCustomAdSticker(null, AdSticker.Type.SLOGAN, imageBitmap, imageBitmap);
                if (adSticker != null) {
                    Intent intent = new Intent();
                    intent.putExtra(CUSTOM_TEXT, adSticker);
                    setResult(RESULT_OK, intent);
                    EventBus.getDefault().post(new AddStickerDoneEvent());
                    finish();
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
            rightTitle.setEnabled(true);
            rightTitle.setTextColor(getResources().getColor(R.color.title_color));
        } else {
            mCustomText = null;
            rightTitle.setEnabled(false);
            rightTitle.setTextColor(getResources().getColor(R.color.no_enable));
            textVertical.setText(null);
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
}
