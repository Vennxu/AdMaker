package com.ekuater.admaker.ui.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.ekuater.admaker.R;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.fragment.image.ImageSelectFragment;
import com.ekuater.admaker.ui.fragment.image.ImageSelectListener;
import com.ekuater.admaker.util.L;
import com.ekuater.admaker.util.TextUtil;

/**
 * Created by Leo on 2015/7/11.
 *
 * @author Leo
 */
public class SelectPhotoActivity extends BackIconActivity implements ImageSelectListener {

    private static final String TAG = SelectPhotoActivity.class.getSimpleName();

    public static final String EXTRA_PHOTO_PATH = "photo_path";
    public static final String EXTRA_IS_TEMP = "isTemp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_fragment_container);
        showImageSelectFragment();
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
        ImageSelectFragment fragment = ImageSelectFragment.newInstance(getString(R.string.select_image), false);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment,
                ImageSelectFragment.class.getSimpleName());
        transaction.commitAllowingStateLoss();
    }

    private void onPhotoSelectSuccess(String photoPath, boolean isTemp) {
        L.v(TAG, "onPhotoSelectSuccess(), photoPath=%1$s, isTemp=%2$s", photoPath, isTemp);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_IS_TEMP, isTemp);
        intent.putExtra(EXTRA_PHOTO_PATH, photoPath);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onPhotoSelectFailure() {
        L.v(TAG, "onPhotoSelectFailure()");
    }
}
