package com.ekuater.admaker.ui.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ekuater.admaker.EnvConfig;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.eventbus.PortfolioPublishedEvent;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.NormalCallListener;
import com.ekuater.admaker.delegate.PortfolioManager;
import com.ekuater.admaker.ui.ContentSharer;
import com.ekuater.admaker.ui.ShareContent;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.fragment.ConfirmDialogFragment;
import com.ekuater.admaker.ui.fragment.LoginDialogFragment;
import com.ekuater.admaker.ui.fragment.ShareDialogFragment;
import com.ekuater.admaker.ui.fragment.SimpleProgressHelper;
import com.ekuater.admaker.ui.util.ScreenUtils;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.ui.widget.DrawableCenterTextView;
import com.ekuater.admaker.util.PhotoSaver;
import com.pnikosis.materialishprogress.ProgressWheel;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/8/12.
 */
public class PushHotissuesActivity extends BackIconActivity implements View.OnClickListener, Handler.Callback {

    private static final int PUSH_SUCCESS = 101;
    private static final int PUSH_FAILED = 102;
    private static final int LOAD_BITMAP_SUCCESS = 103;
    private static final int LOAD_BITMAP_FAILED = 104;

    private PortfolioManager mPortfolioManager;
    private AdElementDisplay mAdElementDisplay;
    private DrawableCenterTextView mPush;
    private DrawableCenterTextView mSave;
    private DrawableCenterTextView mResutl;
    private DrawableCenterTextView mShare;
    private ProgressWheel mProgress;
    private ImageView mImageView;
    private ImageView mCustomFinish;
    private LinearLayout mImageArea;

    private Handler handler = new Handler(this);
    private boolean isBack;
    private ShareDialogFragment dialogFragment;
    private ContentSharer mContentSharer;
    private SimpleProgressHelper simpleProgressHelper;
    private EventBus eventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_hotissues);
        setHasContentSharer();
        mContentSharer = getContentSharer();
        mPortfolioManager = PortfolioManager.getInstance(this);
        mAdElementDisplay = AdElementDisplay.getInstance(this);
        simpleProgressHelper = new SimpleProgressHelper(this);
        eventBus = UIEventBusHub.getDefaultEventBus();
        initView();
    }

    private void initView() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(getString(R.string.finish_hot_image));
        findViewById(R.id.icon).setOnClickListener(this);
        mPush = (DrawableCenterTextView) findViewById(R.id.push);
        mShare = (DrawableCenterTextView) findViewById(R.id.share);
        mResutl = (DrawableCenterTextView) findViewById(R.id.edit);
        mSave = (DrawableCenterTextView) findViewById(R.id.save);
        mImageView = (ImageView) findViewById(R.id.push_hotissues_image);
        mCustomFinish = (ImageView) findViewById(R.id.push_hotissues_finish);
        mProgress = (ProgressWheel) findViewById(R.id.push_hotissues_progress);
        mImageArea = (LinearLayout) findViewById(R.id.push_hotissues_area);
        mProgress.setVisibility(View.GONE);
        mPush.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mResutl.setOnClickListener(this);
        mSave.setOnClickListener(this);
        mearaLayout();
        String url = EnvConfig.genHotImageFile(CustomTextHotImageActivity.HOTIMAGE_URL).getPath();
        if (url != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(url);
            if (bitmap != null) {
                mImageView.setImageBitmap(bitmap);
//                mCustomFinish.setVisibility(View.VISIBLE);
            }
        }
    }

    private void mearaLayout() {
        int width = ScreenUtils.getScreenWidth(this);
        float scale = (float) 2 / 3;
        int height = (int) (width * scale);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mImageView.getLayoutParams();
        layoutParams.height = height;
//        LinearLayout.LayoutParams customImageLayout = (LinearLayout.LayoutParams) mCustomFinish.getLayoutParams();
//        int top = ((mImageArea.getMeasuredHeight() - height)/2) - mCustomFinish.get;
//        customImageLayout.setMargins(0, top, 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.icon:
                finish();
                break;
            case R.id.push:
                if (isLogin()) {
                    pushCheckState();
                } else {
                    showLoginDialog(pushListener);
                }
                break;
            case R.id.share:
                showFragmentDialog();
                break;
            case R.id.save:
                saveBitmap();
                break;
            case R.id.edit:
                showDialog(getString(R.string.revert));
                break;
            default:
                break;
        }
    }

    private void pushCheckState() {
        BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
        if (drawable != null) {
            Bitmap bitmap = drawable.getBitmap();
            if (bitmap != null) {
                pushPortfolio(bitmap);
            }
        }
    }

    private void pushPortfolio(Bitmap bitmap) {
        simpleProgressHelper.show();
        mPortfolioManager.publishPortfolio(bitmap, "", new NormalCallListener() {
            @Override
            public void onCallResult(boolean success) {
                handler.obtainMessage(success ? PUSH_SUCCESS : PUSH_FAILED).sendToTarget();
            }
        });
    }

    private LoginDialogFragment.PushListener pushListener = new LoginDialogFragment.PushListener() {
        @Override
        public void loginFinish() {
            pushCheckState();
        }
    };

    @Override
    public boolean handleMessage(Message msg) {
        boolean handle = true;
        switch (msg.what) {
            case PUSH_SUCCESS:
                simpleProgressHelper.dismiss();
                eventBus.post(new PortfolioPublishedEvent());
                Toast.makeText(this, getString(R.string.push_success), Toast.LENGTH_SHORT).show();
                break;
            case PUSH_FAILED:
                simpleProgressHelper.dismiss();
                Toast.makeText(this, getString(R.string.push_failed), Toast.LENGTH_SHORT).show();
                break;
            case LOAD_BITMAP_SUCCESS:
                mProgress.setVisibility(View.GONE);
                Bitmap[] bitmaps = (Bitmap[]) msg.obj;
                if (bitmaps != null && bitmaps.length > 0) {
                    mImageView.setImageBitmap(bitmaps[0]);
                }
                break;
            case LOAD_BITMAP_FAILED:
                mProgress.setVisibility(View.GONE);
                break;
            default:
                handle = false;
                break;
        }
        return handle;
    }

    private void saveBitmap() {
        BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
        if (drawable != null) {
            Bitmap sceneBitmap = drawable.getBitmap();
            if (sceneBitmap != null) {
                PhotoSaver.savePhoto(this, sceneBitmap, new PhotoSaver.OnSaveListener() {
                    @Override
                    public void onSaveCompleted(final String path) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast(R.drawable.emoji_smile, getString(R.string.saved) + path);
                                if (isBack) {
                                    finish();
                                    isBack = false;
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    private void showToast(@DrawableRes int iconId, @StringRes String stringId) {
        ShowToast.makeText(this, iconId, stringId).show();
    }

    private void cropAdContentImage() {
        UILauncher.launchSelectHotImageUI(this);
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener = new ConfirmDialogFragment.AbsConfirmListener() {
        @Override
        public void onConfirm() {
            isBack = true;
            saveBitmap();
            cropAdContentImage();
        }

        @Override
        public void onCancel() {
            super.onCancel();
            cropAdContentImage();
            finish();
        }

    };

    private void showDialog(String message) {
        ConfirmDialogFragment.UiConfig uiConfig = new ConfirmDialogFragment.UiConfig(message, null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getSupportFragmentManager(), "ConfirmDialogFragment");
    }

    private void showFragmentDialog() {
        dialogFragment = new ShareDialogFragment() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.share_friend:
                        shareBitmapMedia(ShareContent.Platform.WEIXIN);
                        break;
                    case R.id.share_cirle:
                        shareBitmapMedia(ShareContent.Platform.WEIXIN_CIRCLE);
                        break;
                    case R.id.share_qq_friend:
                        shareBitmapMedia(ShareContent.Platform.QQ);
                        break;
                    case R.id.share_xina:
                        shareBitmapMedia(ShareContent.Platform.SINA_WEIBO);
                        break;
                    default:
                        break;
                }
            }
        };
        dialogFragment.show(getSupportFragmentManager(), "ShareDialogFragment");
    }

    private void shareBitmapMedia(ShareContent.Platform platform) {
        Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        ShareContent shareContent = new ShareContent();

        shareContent.setSharePlatform(platform);
        shareContent.setShareBitmap(bitmap);
        shareContent.setTitle(getString(R.string.look));
        shareContent.setUrl("www.ekuater.com");
        mContentSharer.directShareContent(shareContent);
    }
}
