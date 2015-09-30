package com.ekuater.admaker.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ekuater.admaker.EnvConfig;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.AsyncBitmap;
import com.ekuater.admaker.datastruct.Scene;
import com.ekuater.admaker.datastruct.eventbus.FinishEvent;
import com.ekuater.admaker.datastruct.eventbus.PortfolioPublishedEvent;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.NormalCallListener;
import com.ekuater.admaker.delegate.PortfolioManager;
import com.ekuater.admaker.delegate.SceneManager;
import com.ekuater.admaker.ui.ContentSharer;
import com.ekuater.admaker.ui.ShareContent;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.fragment.AdvertisementFragment;
import com.ekuater.admaker.ui.fragment.ConfirmDialogFragment;
import com.ekuater.admaker.ui.fragment.LoginDialogFragment;
import com.ekuater.admaker.ui.fragment.ShareDialogFragment;
import com.ekuater.admaker.ui.fragment.SimpleProgressHelper;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.util.PhotoSaver;
import com.ekuater.admaker.util.TextUtil;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/7/16.
 *
 * @author XuWenxiang
 */
public class OperationAdvertiseActivity extends BackIconActivity
        implements View.OnClickListener, Handler.Callback {

    public final static String OPERATION_SCENE = "scene";
    public final static String OPERATION_ADVERTISE_URL = "advertise_url";

    private static final int SCENE_LOAD_CHANGE_SCENED = 101;
    private static final int SCENE_LOAD_IMAGE_SUCCESS = 102;
    private static final int SCENE_LOAD_IMAGE_FAILED = 103;
    private static final int PUSH_SUCCESS = 104;
    private static final int PUSH_FAILED = 105;

    private ImageView mSceneImage;
    private ImageView mCustomFinish;
    private ProgressWheel mProgress;
    private ContentSharer mContentSharer;
    private Handler looperHandler;
    private HandlerThread handlerThread;
    private Handler mHandler = new Handler(this);
    private String mAdvertiseUrl = null;
    private Scene mScene = null;
    private EventBus mEventBus;
    private boolean isBack;
    private ShareDialogFragment dialogFragment;
    private SimpleProgressHelper mSimpleProgressHelper;
    private PortfolioManager mPortfolioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);
        mEventBus = UIEventBusHub.getDefaultEventBus();
        mSimpleProgressHelper = new SimpleProgressHelper(this);
        mPortfolioManager = PortfolioManager.getInstance(this);
        setHasContentSharer();
        mContentSharer = getContentSharer();
        handlerThread = new HandlerThread("scene_thread",
                android.os.Process.THREAD_PRIORITY_DEFAULT);
        handlerThread.start();
        looperHandler = new Handler(handlerThread.getLooper(), this);
        initOperationView();
        initOperationData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerThread.getLooper().quit();
        handlerThread.quit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialogFragment != null) {
            dialogFragment.dismissAllowingStateLoss();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        reloadAdImages(intent);
    }

    private void loadImage() {
        if (mScene == null || TextUtil.isEmpty(mAdvertiseUrl)) {
            return;
        }
        AdElementDisplay.getInstance(this).loadSceneImages(mScene,
                new AdElementDisplay.BitmapLoadListener() {
                    @Override
                    public void onLoaded(Object object, boolean success, Bitmap[] bitmaps) {
                        mHandler.obtainMessage(success ? SCENE_LOAD_IMAGE_SUCCESS
                                : SCENE_LOAD_IMAGE_FAILED, bitmaps).sendToTarget();
                    }
                });
    }

    private void initOperationData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mScene = intent.getParcelableExtra(OPERATION_SCENE);
        reloadAdImages(intent);
    }

    private void reloadAdImages(Intent intent) {
        mAdvertiseUrl = intent.getStringExtra(OPERATION_ADVERTISE_URL);
        loadImage();
    }

    private void initOperationView() {
        mSceneImage = (ImageView) findViewById(R.id.advertise_operation_image);
        mCustomFinish = (ImageView) findViewById(R.id.advertise_custom_finish);
        mProgress = (ProgressWheel) findViewById(R.id.advertise_operation_progress);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(getString(R.string.endorsement_hot_image));
        findViewById(R.id.edit).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);
        findViewById(R.id.save).setOnClickListener(this);
        findViewById(R.id.push).setOnClickListener(this);
        findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private AsyncBitmap getAsyncBitmap(String url) {
        AsyncBitmap adBitmap = null;

        try {
            Bitmap bitmap = getBitmap(url);
            if (bitmap != null) {
                adBitmap = new AsyncBitmap(bitmap);
            }
        } catch (OutOfMemoryError outOfMemoryError) {
            System.gc();
        } finally {
            //noinspection ResultOfMethodCallIgnored
            new File(url).delete();
        }

        return adBitmap;
    }

    private Bitmap getBitmap(String path) {
        return BitmapFactory.decodeFile(path, null);
    }

    private void getSceneBitmap(Scene scene, AsyncBitmap sceneBitmap, AsyncBitmap advertiseBitmap) {
        looperHandler.post(new ComposeSceneTask(scene, sceneBitmap, advertiseBitmap));
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case SCENE_LOAD_CHANGE_SCENED: {
                Bitmap sceneBitmap = (Bitmap) msg.obj;
                if (sceneBitmap != null) {
//                    mCustomFinish.setVisibility(View.VISIBLE);
                    mSceneImage.setImageBitmap(sceneBitmap);
                }
                break;
            }
            case SCENE_LOAD_IMAGE_SUCCESS: {
                mProgress.setVisibility(View.GONE);
                onHandlerLoadImageResult(msg);
                break;
            }
            case SCENE_LOAD_IMAGE_FAILED: {
                showToast(R.drawable.emoji_cry, R.string.load_image_failed);
                break;
            }
            case PUSH_SUCCESS: {
                mSimpleProgressHelper.dismiss();
                mEventBus.post(new PortfolioPublishedEvent());
                Toast.makeText(this, getString(R.string.push_success), Toast.LENGTH_SHORT).show();
                break;
            }
            case PUSH_FAILED: {
                mSimpleProgressHelper.dismiss();
                Toast.makeText(this, getString(R.string.push_failed), Toast.LENGTH_SHORT).show();
                break;
            }
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void onHandlerLoadImageResult(Message msg) {
        Bitmap[] bitmaps = (Bitmap[]) msg.obj;
        if (bitmaps == null) {
            return;
        }
        Bitmap bitmap = bitmaps[0];
        if (bitmap != null) {
            AsyncBitmap sceneBitmap = new AsyncBitmap(bitmap);
            AsyncBitmap advertiseBitmap = getAsyncBitmap(mAdvertiseUrl);

            if (advertiseBitmap == null) {
                return;
            }
            getSceneBitmap(mScene, sceneBitmap, advertiseBitmap);
        }
    }

    private class ComposeSceneTask implements Runnable {

        private Scene scene;
        private AsyncBitmap sceneBitmap;
        private AsyncBitmap adBitmap;

        public ComposeSceneTask(Scene scene, AsyncBitmap sceneBitmap, AsyncBitmap adBitmap) {
            this.scene = scene;
            this.sceneBitmap = sceneBitmap;
            this.adBitmap = adBitmap;
        }

        @Override
        public void run() {
            Bitmap resultBitmap = null;
            try {
                this.sceneBitmap.lock();
                this.adBitmap.lock();
                resultBitmap = SceneManager.getInstance(OperationAdvertiseActivity.this)
                        .setSceneBitmap(this.adBitmap.getBitmap(),
                                this.scene, this.sceneBitmap.getBitmap());
            } catch (Exception | OutOfMemoryError e) {
                System.gc();
            } finally {
                this.sceneBitmap.unlock();
                this.adBitmap.unlock();
                mHandler.obtainMessage(SCENE_LOAD_CHANGE_SCENED, resultBitmap).sendToTarget();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit:
                showDialog();
                break;
            case R.id.share:
                showFragmentDialog();
                break;
            case R.id.save:
                saveBitmap();
                break;
            case R.id.push:
                if (isLogin()) {
                    pushCheckState();
                } else {
                    showLoginDialog(pushListener);
                }
                break;
            default:
                break;
        }
    }

    private LoginDialogFragment.PushListener pushListener = new LoginDialogFragment.PushListener() {
        @Override
        public void loginFinish() {
            pushCheckState();
        }
    };

    private void pushCheckState() {
        BitmapDrawable drawable = (BitmapDrawable) mSceneImage.getDrawable();
        if (drawable != null) {
            Bitmap bitmap = drawable.getBitmap();
            if (bitmap != null) {
                pushPortfolio(bitmap);
            }
        }
    }

    private void pushPortfolio(Bitmap bitmap) {
        mSimpleProgressHelper.show();
        mPortfolioManager.publishPortfolio(bitmap, "", new NormalCallListener() {
            @Override
            public void onCallResult(boolean success) {
                mHandler.obtainMessage(success ? PUSH_SUCCESS : PUSH_FAILED).sendToTarget();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AdvertisementFragment.CROP_PHOTO_REQUEST:
                onCropAdContentImageResult(resultCode, data);
                break;
            default:
                break;
        }
    }

    private void saveBitmap() {
        BitmapDrawable drawable = (BitmapDrawable) mSceneImage.getDrawable();
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

    private void showToast(@DrawableRes int iconId, @StringRes int stringId) {
        ShowToast.makeText(this, iconId, getString(stringId)).show();
    }

    private void showToast(@DrawableRes int iconId, String string) {
        ShowToast.makeText(this, iconId, string).show();
    }

    private void shareBitmapMedia(ShareContent.Platform platform) {
        Bitmap bitmap = ((BitmapDrawable) mSceneImage.getDrawable()).getBitmap();
        ShareContent shareContent = new ShareContent();

        shareContent.setSharePlatform(platform);
        shareContent.setShareBitmap(bitmap);
        shareContent.setTitle(getString(R.string.look));
        shareContent.setUrl("www.ekuater.com");
        mContentSharer.directShareContent(shareContent);
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

    private void cropAdContentImage() {
        UILauncher.launchMainActivity(this);
    }

    private void onCropAdContentImageResult(int resultCode, Intent data) {
        File tempFile = EnvConfig.genTempFile(AdvertisementFragment.IMAGE_URL);
        if (resultCode == Activity.RESULT_OK && tempFile != null) {
            UILauncher.launchMakeAdvertiseUI(this, mScene,
                    tempFile.getPath(), data.getData());
            // finish();
        }
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener = new ConfirmDialogFragment.AbsConfirmListener() {
        @Override
        public void onConfirm() {
            mEventBus.post(new FinishEvent());
            isBack = true;
            saveBitmap();
            cropAdContentImage();
        }

        @Override
        public void onCancel() {
            super.onCancel();
            mEventBus.post(new FinishEvent());
            cropAdContentImage();
            finish();
        }

    };

    private void showDialog() {
        ConfirmDialogFragment.UiConfig uiConfig = new ConfirmDialogFragment.UiConfig(getString(R.string.revert), null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getSupportFragmentManager(), "ConfirmDialogFragment");
    }
}
