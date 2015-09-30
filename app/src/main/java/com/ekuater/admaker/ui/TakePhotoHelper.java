package com.ekuater.admaker.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.ekuater.admaker.ui.activity.SelectPhotoActivity;
import com.ekuater.admaker.ui.util.BitmapUtils;
import com.ekuater.admaker.util.L;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by Leo on 2015/7/10.
 *
 * @author LinYong
 */
public class TakePhotoHelper {

    public interface TakeListener {
        void onToken(@Nullable Bitmap bitmap);
    }

    private static final String TAG = TakePhotoHelper.class.getSimpleName();
    private static final int REQUEST_TAKE_PHOTO = 50;

    private final Object mActivityStarter;
    private final TakeListener mListener;

    @SuppressWarnings("unused")
    public TakePhotoHelper(@NonNull Fragment fragment,
                           @NonNull TakeListener listener) {
        this((Object) fragment, listener);
    }

    @SuppressWarnings("unused")
    public TakePhotoHelper(@NonNull Activity activity,
                           @NonNull TakeListener listener) {
        this((Object) activity, listener);
    }

    protected TakePhotoHelper(@NonNull Object activityStarter,
                              @NonNull TakeListener listener) {
        mActivityStarter = activityStarter;
        mListener = listener;
    }

    public void takePhoto() {
        onTakePhoto();
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            onPhotoTaken(resultCode, data);
            return true;
        } else {
            return false;
        }
    }

    private void onTakePhoto() {
        Intent intent = new Intent(getContext(), SelectPhotoActivity.class);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private Context getContext() {
        if (mActivityStarter instanceof Activity) {
            return (Context) mActivityStarter;
        } else if (mActivityStarter instanceof Fragment) {
            return ((Fragment) mActivityStarter).getActivity();
        } else {
            return null;
        }
    }

    private void onPhotoTaken(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            final String path = data.getStringExtra(SelectPhotoActivity.EXTRA_PHOTO_PATH);
            final boolean isTemp = data.getBooleanExtra(SelectPhotoActivity.EXTRA_IS_TEMP, false);

            if (!TextUtils.isEmpty(path)) {
                new AsyncTask<Void, Void, Bitmap>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        return BitmapUtils.getSampledBitmap(path, 1000, 1000);
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if (isTemp) {
                            //noinspection ResultOfMethodCallIgnored
                            new File(path).delete();
                        }
                        mListener.onToken(bitmap);
                    }
                }.execute((Void) null);
            }
        }
    }

    private void startActivityForResult(Intent intent, int requestCode) {
        try {
            Class<?> clazz = mActivityStarter.getClass();
            Method method = clazz.getMethod("startActivityForResult",
                    Intent.class, int.class);
            method.invoke(mActivityStarter, intent, requestCode);
        } catch (Exception e) {
            L.w(TAG, e);
        }
    }
}
