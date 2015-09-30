package com.ekuater.admaker.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.ui.activity.CropPhotoActivity;
import com.ekuater.admaker.ui.util.BitmapUtils;
import com.ekuater.admaker.ui.util.Matrix3;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.ui.widget.ImageViewTouch;
import com.ekuater.admaker.ui.widget.ImageViewTouchBase;
import com.ekuater.admaker.ui.widget.StickerItem;
import com.ekuater.admaker.ui.widget.StickerType;
import com.ekuater.admaker.ui.widget.StickerView;
import com.ekuater.admaker.util.L;

import java.io.File;

/**
 * Created by Leo on 2015/6/1.
 *
 * @author LinYong
 */
public class AdWorkspaceFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = AdWorkspaceFragment.class.getSimpleName();

    private static final int REQUEST_CROP_PHOTO = 10;

    private interface Notifier {
        void notify(AdWorkspaceListener listener);
    }

    private Bitmap mMainBitmap;
    private LoadImageTask mLoadImageTask;
    private AdWorkspaceListener mListener;

    private ImageViewTouch mMainImage;
    private StickerView mStickerView;
    private View mSelectImageView;
    private SimpleProgressHelper mProgressHelper;
    private AdElementDisplay mAdElementDisplay;
    private LinearLayout mMaskLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressHelper = new SimpleProgressHelper(this);
        mAdElementDisplay = AdElementDisplay.getInstance(getActivity());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AdWorkspaceListener) activity;
        } catch (ClassCastException e) {
            L.w(TAG, "onAttach(), class cast to listener failed.");
            mListener = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ad_workspace, container, false);
        mMainImage = (ImageViewTouch) rootView.findViewById(R.id.main_image);
        mStickerView = (StickerView) rootView.findViewById(R.id.sticker_panel);
        mSelectImageView = rootView.findViewById(R.id.select_image);
        mMaskLayout = (LinearLayout) rootView.findViewById(R.id.image_mask);
        mMainImage.setScaleable(false);
        mSelectImageView.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }
        if (mMainBitmap != null && !mMainBitmap.isRecycled()) {
            mMainBitmap.recycle();
        }
        mStickerView.clear();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CROP_PHOTO:
                onCropPhoto(resultCode, data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_image:
                startCropPhoto();
                break;
            default:
                break;
        }
    }

    public void addSticker(@NonNull final AdSticker sticker) {
        Activity activity = getActivity();
        if (activity != null) {
            mProgressHelper.show();
            mAdElementDisplay.loadStickerImages(sticker, new AdElementDisplay.BitmapLoadListener() {
                @Override
                public void onLoaded(Object object, boolean success, Bitmap[] bitmaps) {
                    mProgressHelper.dismiss();
                    if (success) {
                        mStickerView.addStickerImage(sticker.getType() == AdSticker.Type.SLOGAN
                                        ? StickerType.SLOGAN : StickerType.TRADEMARK,
                                bitmaps[0], bitmaps[1]);
                    } else if (getActivity() != null) {
                        ShowToast.makeText(getActivity(), R.drawable.emoji_sad,
                                getString(R.string.load_sticker_failed)).show();
                    }
                }
            });
        }
    }

    public void saveStickers() {
        if (mMainBitmap != null && !mMainBitmap.isRecycled()) {
            new SaveStickersTask().execute((Void) null);
        }
    }

    private void notifyListener(Notifier notifier) {
        if (mListener != null && notifier != null) {
            notifier.notify(mListener);
        }
    }

    private void startCropPhoto() {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, CropPhotoActivity.class);
            startActivityForResult(intent, REQUEST_CROP_PHOTO);
        }
    }

    public void onCropPhoto(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            //noinspection LoopStatementThatDoesntLoop
            do {
                Uri uri = data.getData();
                if (uri == null) {
                    break;
                }

                String path = uri.getPath();
                if (TextUtils.isEmpty(path)) {
                    break;
                }

                File file = new File(path);
                if (!file.exists()) {
                    break;
                }

                loadImage(file);
                return;
            } while (false);

            mSelectImageView.setVisibility(View.VISIBLE);
        }
    }

    private void loadImage(File imageFile) {
        Activity activity = getActivity();
        if (activity != null) {
            if (mLoadImageTask != null) {
                mLoadImageTask.cancel(true);
            }
            mLoadImageTask = new LoadImageTask(activity);
            mLoadImageTask.execute(imageFile.getAbsolutePath());
        }
    }

    private void changeMainBitmap(Bitmap mainBitmap) {
        if (mainBitmap != mMainBitmap) {
            if (mMainBitmap != null && !mMainBitmap.isRecycled()) {
                mMainBitmap.recycle();
            }
            mMainBitmap = mainBitmap;
            mMainImage.setImageBitmap(mMainBitmap);
            mMainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        }
    }

    private void setupImageMask(final int imageWidth, final int imageHeight) {
        if (mMaskLayout.getWidth() <= 0) {
            mMaskLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    mMaskLayout.removeOnLayoutChangeListener(this);
                    setupImageMaskInternal(imageWidth, imageHeight);
                }
            });
        } else {
            setupImageMaskInternal(imageWidth, imageHeight);
        }
    }

    private void setupImageMaskInternal(final int imageWidth, final int imageHeight) {
        final int width = mMaskLayout.getWidth();
        final int height = mMaskLayout.getHeight();
        final float scale = Math.min((float) width / imageWidth, (float) height / imageHeight);
        final float hGap = width - imageWidth * scale;
        final float vGap = height - imageHeight * scale;
        final Context context = mMaskLayout.getContext();

        mMaskLayout.removeAllViews();
        if (hGap > 0 && hGap > vGap) {
            mMaskLayout.setOrientation(LinearLayout.HORIZONTAL);
            mMaskLayout.addView(newMaskView(context), new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            mMaskLayout.addView(newSpaceView(context), new LinearLayout.LayoutParams(
                    Math.round(imageWidth * scale), ViewGroup.LayoutParams.MATCH_PARENT));
            mMaskLayout.addView(newMaskView(context), new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        } else if (vGap > 0 && vGap > hGap) {
            mMaskLayout.setOrientation(LinearLayout.VERTICAL);
            mMaskLayout.addView(newMaskView(context), new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
            mMaskLayout.addView(newSpaceView(context), new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Math.round(imageHeight * scale)));
            mMaskLayout.addView(newMaskView(context), new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        } else {
            mMaskLayout.setOrientation(LinearLayout.VERTICAL);
        }

        mStickerView.setTargetSize(new Point(Math.round(imageWidth * scale),
                Math.round(imageHeight * scale)));
    }

    private View newMaskView(Context context) {
        View maskView = new View(context);
        maskView.setBackgroundColor(0xCF000000);
        return maskView;
    }

    private Space newSpaceView(Context context) {
        return new Space(context);
    }

    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        private Resources resources;
        private int imageWidth;
        private int imageHeight;

        public LoadImageTask(Context context) {
            resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            imageWidth = (int) ((float) metrics.widthPixels / 1.5);
            imageHeight = (int) ((float) metrics.heightPixels / 1.5);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String path = params[0];

            for (int i = 0; i < 3; ++i) {
                try {
                    return load(path);
                } catch (OutOfMemoryError error) {
                    System.gc();
                }
            }
            return null;
        }

        private Bitmap load(String path) {
            Bitmap bitmap = BitmapUtils.loadImageByPath(path, imageWidth, imageHeight);
            File file = new File(path);
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            return bitmap;
        }

        @Override
        protected void onPreExecute() {
            mProgressHelper.show();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mProgressHelper.dismiss();
            mLoadImageTask = null;

            if (bitmap == null || bitmap.isRecycled()) {
                if (getActivity() != null) {
                    ShowToast.makeText(getActivity(), R.drawable.emoji_sad,
                            getString(R.string.load_image_failed)).show();
                }
                return;
            }

            changeMainBitmap(bitmap);
            mSelectImageView.setVisibility(View.GONE);
            setupImageMask(bitmap.getWidth(), bitmap.getHeight());
            notifyListener(new Notifier() {
                @Override
                public void notify(AdWorkspaceListener listener) {
                    listener.onBaseImageReady(mMainBitmap);
                }
            });
        }
    }

    private class SaveStickersTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                return compose();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private Bitmap compose() {
            Bitmap resultBmp = mMainBitmap.copy(Bitmap.Config.ARGB_8888, true);
            if (resultBmp == null) {
                System.gc();
                return null;
            }

            Matrix touchMatrix = mMainImage.getImageViewMatrix();
            Canvas canvas = new Canvas(resultBmp);
            Paint paint = new Paint();
            float[] data = new float[9];

            touchMatrix.getValues(data);// 底部图片变化记录矩阵原始数据
            Matrix3 cal = new Matrix3(data);// 辅助矩阵计算类
            Matrix3 inverseMatrix = cal.inverseMatrix();// 计算逆矩阵
            Matrix m = new Matrix();
            m.setValues(inverseMatrix.getValues());
            paint.setAntiAlias(true);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                    Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

            for (StickerItem item : mStickerView.getBank()) {
                item.matrix.postConcat(m);// 乘以底部图片变化矩阵
                canvas.drawBitmap(item.curBitmap, item.matrix, paint);
            }

            return resultBmp;
        }

        @Override
        protected void onPreExecute() {
            mProgressHelper.show();
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            mProgressHelper.dismiss();

            if (bitmap != null) {
                mStickerView.clear();
                notifyListener(new Notifier() {
                    @Override
                    public void notify(AdWorkspaceListener listener) {
                        listener.onSaveStickersDone(bitmap);
                    }
                });
                changeMainBitmap(bitmap);
            } else if (getActivity() != null) {
                ShowToast.makeText(getActivity(), R.drawable.emoji_sad,
                        getString(R.string.compose_stickers_failed)).show();
            }
        }
    }
}
