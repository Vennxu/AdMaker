package com.ekuater.admaker.ui.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;

import com.ekuater.admaker.EnvConfig;
import com.ekuater.admaker.R;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.fragment.image.ImageSelectFragment;
import com.ekuater.admaker.ui.fragment.image.ImageSelectListener;
import com.ekuater.admaker.util.L;
import com.ekuater.admaker.util.TextUtil;

import java.io.File;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

/**
 * Created by Leo on 2015/5/16.
 *
 * @author LinYong
 */
public class CropPhotoActivity extends BackIconActivity implements ImageSelectListener {

    private static final String TAG = CropPhotoActivity.class.getSimpleName();

    public static final String CROP_PHOTO_TYPE = "crop_photo";
    public static final String CROP_SIZE_ARG = "crop_size_arg";
    public static final String CROP_PHOTO_PROGRESS = "is_show_progress";
    public static final String CROP_TITLE = "title";

    public static final int SYSTEM_CROP_PHOTO = 0;
    public static final int CUSTOM_DROP_PHOTO = 1;

    private static final String ACTION_IMAGE_CROP = "com.android.camera.action.CROP";

    private static final int REQUEST_CROP_AVATAR = 11;
    private static final int REQUEST_CUSTOM_CROP_AVATAR = 12;

    private String mTempPhotoFile;
    private Uri mCroppedImageUri;
    private int mType;
    private Point mCropSize;
    private boolean isShowProgress;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
//        getSwipeBackLayout().setEnableGesture(false);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mType = getIntent().getIntExtra(CROP_PHOTO_TYPE, 0);
        mCropSize = getIntent().getParcelableExtra(CROP_SIZE_ARG);
        isShowProgress = getIntent().getBooleanExtra(CROP_PHOTO_PROGRESS, false);
        title = getIntent().getStringExtra(CROP_TITLE);

        setContentView(R.layout.activity_fragment_container);
        showImageSelectFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteTempPhotoFile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CROP_AVATAR:
                onCropAvatarResult(resultCode, data);
                break;
            case REQUEST_CUSTOM_CROP_AVATAR:
                onCustomCropAvatarResult(resultCode, data);
                break;
            default:
                break;
        }
    }

    // For ImageSelectListener
    @Override
    public void onSelectSuccess(String imagePath, boolean isTemp) {
        onPhotoSelectSuccess(imagePath, isTemp);
    }

    @Override
    public void onMultiSelectSuccess(String[] imagePaths) {
        finish();
    }

    @Override
    public void onSelectFailure() {
        onPhotoSelectFailure();
    }
    // End ImageSelectListener

    private void showImageSelectFragment() {
        ImageSelectFragment fragment = ImageSelectFragment.newInstance(
                TextUtil.isEmpty(title) ? getString(R.string.select_image) : title, isShowProgress);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment,
                ImageSelectFragment.class.getSimpleName());
        transaction.commitAllowingStateLoss();
    }

    private void onPhotoSelectSuccess(String photoPath, boolean isTemp) {
        L.v(TAG, "onPhotoSelectSuccess(), photoPath=%1$s, isTemp=%2$s", photoPath, isTemp);
        deleteTempPhotoFile();
        deleteCroppedImage();
        mTempPhotoFile = isTemp ? photoPath : null;
        showAvatar(photoPath);
    }

    private void showAvatar(String photoPath) {
        switch (mType) {
            case SYSTEM_CROP_PHOTO:
                showCropAvatar(Uri.fromFile(new File(photoPath)));
                break;
            case CUSTOM_DROP_PHOTO:
                showCustomCropAvatar(photoPath);
                break;
        }
    }

    private void onPhotoSelectFailure() {
        L.v(TAG, "onPhotoSelectFailure()");
    }

    private void showCropAvatar(Uri uri) {
        int aspectX;
        int aspectY;
        int outputX;
        int outputY;

        if (mCropSize != null && mCropSize.x > 0 && mCropSize.y > 0) {
            aspectX = outputX = mCropSize.x;
            aspectY = outputY = mCropSize.y;
        } else {  // default aspect and size
            aspectX = 2;
            aspectY = 1;
            outputX = 900;
            outputY = 450;
        }
        Intent intent = new Intent(ACTION_IMAGE_CROP);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("noFaceDetection", false);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        mCroppedImageUri = Uri.fromFile(EnvConfig.genTempFile(".jpg"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCroppedImageUri);
        startActivityForResult(intent, REQUEST_CROP_AVATAR);
    }

    private void showCustomCropAvatar(String photoPath) {
        Intent intent = new Intent(this, CuterImageActivity.class);
        intent.putExtra(CuterImageActivity.CUTER_URI, photoPath);
        startActivityForResult(intent, REQUEST_CUSTOM_CROP_AVATAR);
    }

    private void onCropAvatarResult(int resultCode, Intent data) {
        deleteTempPhotoFile();
        if (RESULT_OK == resultCode && data != null) {
            Intent intent = new Intent();
            intent.setData(mCroppedImageUri);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            deleteCroppedImage();
        }
    }

    private void onCustomCropAvatarResult(int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void deleteCroppedImage() {
        if (mCroppedImageUri != null) {
            if (mCroppedImageUri.getScheme().equals("file")) {
                //noinspection ResultOfMethodCallIgnored
                new File(mCroppedImageUri.getPath()).delete();
            }
            mCroppedImageUri = null;
        }
    }

    private void deleteTempPhotoFile() {
        if (mTempPhotoFile != null) {
            //noinspection ResultOfMethodCallIgnored
            new File(mTempPhotoFile).delete();
            mTempPhotoFile = null;
        }
    }
}
