package com.ekuater.admaker.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.admaker.EnvConfig;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.AsyncBitmap;
import com.ekuater.admaker.datastruct.Scene;
import com.ekuater.admaker.datastruct.eventbus.PortfolioPublishedEvent;
import com.ekuater.admaker.datastruct.eventbus.PublishNotLoginEvent;
import com.ekuater.admaker.delegate.AccountManager;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.AdResLoadListener;
import com.ekuater.admaker.delegate.AdResLoader;
import com.ekuater.admaker.delegate.NormalCallListener;
import com.ekuater.admaker.delegate.PortfolioManager;
import com.ekuater.admaker.delegate.SceneManager;
import com.ekuater.admaker.ui.ContentSharer;
import com.ekuater.admaker.ui.ShareContent;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.AdvertiseActivity;
import com.ekuater.admaker.ui.activity.base.BaseActivity;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.util.PhotoSaver;
import com.ekuater.admaker.util.TextUtil;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.File;


/**
 * Created by Administrator on 2015/6/1.
 *
 * @author Xu wenxiang
 */
public class AdvertisementFragment extends Fragment
        implements View.OnClickListener, Handler.Callback {

    public static final String IMAGE_URL = "jpg";
    public static final int CROP_PHOTO_REQUEST = 15;

    private static final int SCENE_SUCCEED = 101;
    private static final int SCENE_FAILED = 102;
    private static final int SCENE_LOAD_BITMAP_SUCCESS = 103;
    private static final int SCENE_LOAD_BITMAP_FAILED = 104;
    private static final int SCENE_LOAD_CHANGE_SCENED = 105;
    private static final int SCENE_PUSH_BITMAP_SUCCESS = 106;
    private static final int SCENE_PUSH_BITMAP_FAILED = 107;

    private FrameLayout mFrameLayout;
    private ProgressWheel mProgressBar, mGridProgressBar;
    private GridView mGridView;
    private ImageView mChooseImage;
    private AdvertisementAdapter mAdapter;
    private AsyncBitmap mAdBitmap = null;
    private ContentSharer mContentSharer;
    private SceneManager mSceneManager;
    private AdResLoader mLoader;
    private int mPage = 1;
    private Scene mScene;
    private Context mContext;
    private AdElementDisplay mAdElementDisplay;
    private AsyncBitmap loadBitmap = null;
    private Handler looperHandler;
    private HandlerThread handlerThread;
    private PortfolioManager mPortfolioManager;
    private SimpleProgressHelper mSimpleProgressHelper;

    private Handler mHandler = new Handler(this);

    private AdElementDisplay.BitmapLoadListener loadListener
            = new AdElementDisplay.BitmapLoadListener() {
        @Override
        public void onLoaded(Object object, boolean success, Bitmap[] bitmaps) {
            mHandler.obtainMessage(success ? SCENE_LOAD_BITMAP_SUCCESS : SCENE_LOAD_BITMAP_FAILED,
                    bitmaps).sendToTarget();
        }
    };

    private AdvertisementAdapter.SelectListener selectListener = new AdvertisementAdapter.SelectListener() {
        @Override
        public void onSelect(Scene scene) {
            showSceneImage(scene);
        }
    };

    private void showSceneImage(Scene scene) {
        mScene = scene;
        mFrameLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mAdElementDisplay.loadSceneImages(scene, loadListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handlerThread = new HandlerThread("scene_thread", Process.THREAD_PRIORITY_DEFAULT);
        handlerThread.start();
        looperHandler = new Handler(handlerThread.getLooper(), this);
        mContext = getActivity();
        BaseActivity baseActivity = (BaseActivity) getActivity();
        baseActivity.setHasContentSharer();
        mContentSharer = baseActivity.getContentSharer();
        DisplayMetrics mMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(mMetrics);
        mSceneManager = SceneManager.getInstance(mContext);
        mAdElementDisplay = AdElementDisplay.getInstance(mContext);
        mPortfolioManager = PortfolioManager.getInstance(mContext);
        mSimpleProgressHelper = new SimpleProgressHelper(this);
        mLoader = AdResLoader.getInstance(mContext);
        mAdapter = new AdvertisementAdapter(mContext, mMetrics, selectListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_choose_acene,
                container, false);
        initView(view);
        initData();
        changeGridProgressState();
        getScene();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdBitmap != null) {
            mAdBitmap.recycle();
        }
        if (handlerThread != null) {
            handlerThread.getLooper().quit();
            handlerThread.quit();
        }
    }

    private void cropAdContentImage() {
        File tempFile = EnvConfig.genTempFile(IMAGE_URL);
        if (tempFile != null && mAdapter.getCount() > 0) {
            UILauncher.launchCropPhotoUI(this, mScene.getContentSize(), 0, CROP_PHOTO_REQUEST, true, getString(R.string.select_image));
        }
    }

    private void onCropAdContentImageResult(int resultCode, Intent data) {
        File tempFile = EnvConfig.genTempFile(IMAGE_URL);
        if (resultCode == Activity.RESULT_OK && tempFile != null) {
            UILauncher.launchMakeAdvertiseUI(this, mScene,
                    tempFile.getPath(), data.getData());
        }
    }

    private void initView(View view) {
        initTitle(view);
        mGridView = (GridView) view.findViewById(R.id.choose_grid);
        mChooseImage = (ImageView) view.findViewById(R.id.choose_image);
        mFrameLayout = (FrameLayout) view.findViewById(R.id.choose_image_frame);
        mProgressBar = (ProgressWheel) view.findViewById(R.id.choose_progress);
        mGridProgressBar = (ProgressWheel) view.findViewById(R.id.choose_grid_progress);
        mChooseImage.setOnClickListener(this);
    }

    private void initTitle(View view) {
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
//        icon.setBackgroundResource(R.drawable.ic_back_idea);
        TextView rightTitle = (TextView) view.findViewById(R.id.right_title);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(getString(R.string.select_background));
        rightTitle.setVisibility(View.VISIBLE);
        icon.setVisibility(View.VISIBLE);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 getActivity().finish();
//                UILauncher.launchMainSelectHotImageUI(mContext);
            }
        });
        rightTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropAdContentImage();
            }
        });
    }

    private void initData() {
        mGridView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AdvertiseActivity.RESULT_CODE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String path = data.getStringExtra(AdvertiseActivity.EXTRA_OUTPUT_PATH);
                    if (!TextUtil.isEmpty(path)) {
                        try {
                            Bitmap bitmap = getBitmap(path);
                            if (bitmap != null) {
                                if (mAdBitmap != null) {
                                    mAdBitmap.recycle();
                                }
                                mAdBitmap = new AsyncBitmap(bitmap);
                            }
                        } catch (OutOfMemoryError outOfMemoryError) {
                            System.gc();
                        }
                        //noinspection ResultOfMethodCallIgnored
                        new File(path).delete();
                        getSceneBitmap();
                    }
                }
                break;
            case CROP_PHOTO_REQUEST:
                onCropAdContentImageResult(resultCode, data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choose_image:
                cropAdContentImage();
                break;
            default:
                break;
        }
    }

    private Bitmap getBitmap(String path) {
        return BitmapFactory.decodeFile(path, null);
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
                resultBitmap = mSceneManager.setSceneBitmap(this.adBitmap.getBitmap(),
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

    private void getSceneBitmap() {
        looperHandler.post(new ComposeSceneTask(mScene, loadBitmap, mAdBitmap));
    }

    private void shareFaceBanknote(ShareContent.Platform platform) {
        Bitmap sceneBitmap = ((BitmapDrawable) mChooseImage.getDrawable()).getBitmap();
        ShareContent shareContent = new ShareContent();
        shareContent.setSharePlatform(platform);
        shareContent.setShareBitmap(sceneBitmap);
        mContentSharer.directShareContent(shareContent);
    }

    private void showFragmentDialog() {
        SelectDialogFragment shareDialogFragment = new SelectDialogFragment(R.drawable.ic_share_weixin,
                R.drawable.ic_share_wxcircle,
                getString(R.string.share_to_weixin_friend),
                getString(R.string.share_to_weixin_circle)) {
            @Override
            protected void onFistClick() {
                shareFaceBanknote(ShareContent.Platform.WEIXIN);
            }

            @Override
            protected void onTwoClick() {
                shareFaceBanknote(ShareContent.Platform.WEIXIN_CIRCLE);
            }
        };
        shareDialogFragment.show(getFragmentManager(), "ShareDialogFragment");
    }

    private void getScene() {
        mLoader.loadScenes(mPage, new AdResLoadListener<Scene>() {
            @Override
            public void onLoaded(boolean success, boolean remaining, Scene[] resArray) {
                mHandler.obtainMessage(success ? SCENE_SUCCEED : SCENE_FAILED,
                        resArray).sendToTarget();
            }
        });
    }

    private void saveBitmap() {
        Bitmap sceneBitmap = ((BitmapDrawable) mChooseImage.getDrawable()).getBitmap();
        if (sceneBitmap != null) {
            PhotoSaver.savePhoto(mContext, sceneBitmap, new PhotoSaver.OnSaveListener() {
                @Override
                public void onSaveCompleted(final String path) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ShowToast.makeText(mContext,
                                    R.drawable.emoji_smile,
                                    getString(R.string.saved) + path).show();
                        }
                    });
                }
            });
        }
    }

    private void publishBitmap() {
        if (AccountManager.getInstance(mContext).isLogin()) {
            if (mAdBitmap != null) {
                mSimpleProgressHelper.show();
                Bitmap bitmap = ((BitmapDrawable) mChooseImage.getDrawable()).getBitmap();
                mPortfolioManager.publishPortfolio(bitmap, "", new NormalCallListener() {
                    @Override
                    public void onCallResult(boolean success) {
                        mHandler.obtainMessage(success ? SCENE_PUSH_BITMAP_SUCCESS
                                : SCENE_PUSH_BITMAP_FAILED).sendToTarget();
                    }
                });
            }
        } else {
            UIEventBusHub.getDefaultEventBus().post(new PublishNotLoginEvent());
        }
    }

    private void changeGridProgressState() {
        mGridProgressBar.setVisibility(mGridProgressBar.getVisibility() == View.VISIBLE
                ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case SCENE_SUCCEED:
                onHandlerLoadScene(msg);
                break;
            case SCENE_FAILED:
                showToast(R.string.load_failed, R.drawable.emoji_cry);
                break;
            case SCENE_LOAD_BITMAP_SUCCESS:
                onHandlerLoadBitmap(msg);
                break;
            case SCENE_LOAD_BITMAP_FAILED:
                showToast(R.string.load_failed, R.drawable.emoji_cry);
                break;
            case SCENE_LOAD_CHANGE_SCENED: {
                Bitmap sceneBitmap = (Bitmap) msg.obj;
                if (sceneBitmap != null) {
                    mChooseImage.setImageBitmap(sceneBitmap);
                }
                break;
            }
            case SCENE_PUSH_BITMAP_SUCCESS:
                mSimpleProgressHelper.dismiss();
                showToast(R.string.push_success, R.drawable.emoji_smile);
                UIEventBusHub.getDefaultEventBus().post(new PortfolioPublishedEvent());
                break;
            case SCENE_PUSH_BITMAP_FAILED:
                mSimpleProgressHelper.dismiss();
                showToast(R.string.push_failed, R.drawable.emoji_cry);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void showToast(int stringId, int drawable) {
        if (mContext != null) {
            ShowToast.makeText(mContext, drawable, mContext.getString(stringId)).show();
        }
    }

    private void onHandlerLoadScene(Message msg) {
        if (msg.obj != null) {
            Scene[] scenes = (Scene[]) msg.obj;
            if (scenes.length > 0) {
                mAdapter.addScenes(scenes);
                if (scenes.length < 50) {
                    changeGridProgressState();
                    showSceneImage(mAdapter.getItem(0));
                    mAdapter.notifyDataSetChanged();
                } else {
                    mPage++;
                    getScene();
                }
            }
        }
    }

    private void onHandlerLoadBitmap(Message msg) {
        if (msg.obj != null) {
            Bitmap[] bitmaps = (Bitmap[]) msg.obj;
            loadBitmap = new AsyncBitmap(bitmaps[0]);
            mFrameLayout.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            if (mAdBitmap != null) {
                getSceneBitmap();
            } else {
                mChooseImage.setImageBitmap(loadBitmap.getBitmap());
                mChooseImage.setBackgroundColor(Color.WHITE);
            }
        }
    }
}
