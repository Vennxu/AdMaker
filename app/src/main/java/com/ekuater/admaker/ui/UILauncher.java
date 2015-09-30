
package com.ekuater.admaker.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ekuater.admaker.datastruct.HotIssue;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.datastruct.Scene;
import com.ekuater.admaker.datastruct.SimpleUserVO;
import com.ekuater.admaker.ui.activity.AdvertiseActivity;
import com.ekuater.admaker.ui.activity.CommunityActivity;
import com.ekuater.admaker.ui.activity.CommunityDescriptionActivity;
import com.ekuater.admaker.ui.activity.CropPhotoActivity;
import com.ekuater.admaker.ui.activity.CustomTextActivity;
import com.ekuater.admaker.ui.activity.CustomTextHotImageActivity;
import com.ekuater.admaker.ui.activity.FeedbackActivity;
import com.ekuater.admaker.ui.activity.FragmentContainerActivity;
import com.ekuater.admaker.ui.activity.HomePageActivity;
import com.ekuater.admaker.ui.activity.InputCustomTextActivity;
import com.ekuater.admaker.ui.activity.MainActivityAdOnly;
import com.ekuater.admaker.ui.activity.MainHotImageActivity;
import com.ekuater.admaker.ui.activity.OperationAdvertiseActivity;
import com.ekuater.admaker.ui.activity.PushHotissuesActivity;
import com.ekuater.admaker.ui.activity.SceneFinishActivity;
import com.ekuater.admaker.ui.activity.SelectHotImageActivity;
import com.ekuater.admaker.ui.activity.SelectLoginActivity;
import com.ekuater.admaker.ui.activity.ShowBigImageActivity;

/**
 * Launch all activities here, please do not start activity directly.
 *
 * @author LinYong
 */
public final class UILauncher {

    public static void launchFragmentInNewActivity(Context context,
                                                   Class<? extends Fragment> fragment,
                                                   Bundle arguments) {
        context.startActivity(getFragmentInNewActivity(context, fragment, arguments));
    }

    public static Intent getFragmentInNewActivity(Context context,
                                                  Class<? extends Fragment> fragment,
                                                  Bundle arguments) {
        Intent intent = new Intent(context, FragmentContainerActivity.class);
        intent.putExtra(FragmentContainerActivity.EXTRA_SHOW_FRAGMENT, fragment.getName());
        if (arguments != null) {
            intent.putExtra(FragmentContainerActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,
                    arguments);
        }
        return intent;
    }

    public static void launchStoryShowPhotoUI(Context context, PortfolioVO portfolioVO, int position, String tag) {
        Intent intent = new Intent(context, ShowBigImageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ShowBigImageActivity.EXTRA_BITMAP, portfolioVO);
        bundle.putInt(ShowBigImageActivity.EXTRA_POSITION, position);
        bundle.putString(ShowBigImageActivity.EXTRA_TAG, tag);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void launchMakeAdvertiseUI(Fragment fragment, int requestCode, Scene scene,
                                             String outputPath, Uri imageUri) {
        Activity activity = fragment.getActivity();
        Intent intent = new Intent(activity, AdvertiseActivity.class);
        intent.putExtra(AdvertiseActivity.SCENE, scene);
        intent.putExtra(AdvertiseActivity.EXTRA_OUTPUT_PATH, outputPath);
        intent.setData(imageUri);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void launchMakeAdvertiseUI(Activity activity, Scene scene,
                                             String outputPath, Uri imageUri) {
        Intent intent = new Intent(activity, AdvertiseActivity.class);
        intent.putExtra(AdvertiseActivity.SCENE, scene);
        intent.putExtra(AdvertiseActivity.EXTRA_OUTPUT_PATH, outputPath);
        intent.setData(imageUri);
        activity.startActivity(intent);
    }

    public static void launchMakeAdvertiseUI(Fragment fragment, Scene scene,
                                             String outputPath, Uri imageUri) {
        Activity activity = fragment.getActivity();
        Intent intent = new Intent(activity, AdvertiseActivity.class);
        intent.putExtra(AdvertiseActivity.SCENE, scene);
        intent.putExtra(AdvertiseActivity.EXTRA_OUTPUT_PATH, outputPath);
        intent.setData(imageUri);
        fragment.startActivity(intent);
    }

    public static void launchSceneFinishUI(Activity activity,
                                           String outputPath, Scene scene) {
        Intent intent = new Intent(activity, SceneFinishActivity.class);
        intent.putExtra(AdvertiseActivity.EXTRA_OUTPUT_PATH, outputPath);
        intent.putExtra(AdvertiseActivity.EXTRA_OUTPUT_SCENE, scene);

    }

    public static void launchCustomText(Fragment fragment, int requestCode) {
        Activity activity = fragment.getActivity();
        Intent intent = new Intent(activity, CustomTextActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void launchInputCustomText(Context context, String text) {
        Intent intent = new Intent(context, InputCustomTextActivity.class);
        intent.putExtra(InputCustomTextActivity.CUSTOM_TEXT, text);
        context.startActivity(intent);
    }

    public static void launchSelctLoginActivity(Context context) {
        context.startActivity(new Intent(context, SelectLoginActivity.class));
    }

    public static void launchMainActivity(Context context) {
        context.startActivity(new Intent(context, MainActivityAdOnly.class));
    }

    public static void launchCommunityActivity(Context context) {
        context.startActivity(new Intent(context, CommunityActivity.class));
    }

    public static void launchCommunityDescriptActivity(Context context, PortfolioVO portfolioVO, int position) {
        Intent intent = new Intent(context, CommunityDescriptionActivity.class);
        intent.putExtra(CommunityDescriptionActivity.PORTFOLIOVO, portfolioVO);
        intent.putExtra(CommunityDescriptionActivity.PORTFOLIOVO_INDEX, position);
        context.startActivity(intent);
    }

    public static void launchCropPhotoUI(Fragment fragment, int type, int code, boolean isShowProgress,String title) {
        Intent intent = new Intent(fragment.getActivity(), CropPhotoActivity.class);
        intent.putExtra(CropPhotoActivity.CROP_PHOTO_TYPE, type);
        intent.putExtra(CropPhotoActivity.CROP_PHOTO_PROGRESS, isShowProgress);
        intent.putExtra(CropPhotoActivity.CROP_TITLE, title);
        fragment.startActivityForResult(intent, code);
    }

    public static void launchCropPhotoUI(Fragment fragment, Point point, int type, int code, boolean isShowProgress, String title) {
        Intent intent = new Intent(fragment.getActivity(), CropPhotoActivity.class);
        intent.putExtra(CropPhotoActivity.CROP_SIZE_ARG, point);
        intent.putExtra(CropPhotoActivity.CROP_PHOTO_TYPE, type);
        intent.putExtra(CropPhotoActivity.CROP_PHOTO_PROGRESS, isShowProgress);
        intent.putExtra(CropPhotoActivity.CROP_TITLE, title);
        fragment.startActivityForResult(intent, code);
    }

    public static void launchCropPhotoUI(Activity activity, Point point, int type, int code, String title) {
        Intent intent = new Intent(activity, CropPhotoActivity.class);
        intent.putExtra(CropPhotoActivity.CROP_SIZE_ARG, point);
        intent.putExtra(CropPhotoActivity.CROP_PHOTO_TYPE, type);
        intent.putExtra(CropPhotoActivity.CROP_TITLE, title);
        activity.startActivityForResult(intent, code);
    }

    public static void launchHomePageUI(Context context, SimpleUserVO userVO) {
        Intent intent = new Intent(context, HomePageActivity.class);
        intent.putExtra(HomePageActivity.USER, userVO);
        context.startActivity(intent);
    }

    public static void launchOperationAdvertiseUI(Activity activity, Scene scene, String advertiseUrl) {
        Intent intent = new Intent(activity, OperationAdvertiseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(OperationAdvertiseActivity.OPERATION_SCENE, scene);
        intent.putExtra(OperationAdvertiseActivity.OPERATION_ADVERTISE_URL, advertiseUrl);
        activity.startActivity(intent);
    }

    public static void launchFeedbackUI(Context context) {
        Intent intent = new Intent(context, FeedbackActivity.class);
        context.startActivity(intent);
    }

    public static void launchSelectHotImageUI(Context context) {
        Intent intent = new Intent(context, SelectHotImageActivity.class);
        context.startActivity(intent);
    }

    public static void launchMainSelectHotImageUI(Context context) {
        Intent intent = new Intent(context, MainHotImageActivity.class);
        context.startActivity(intent);
    }

    public static void launchPushHotIsssueUI(Activity activity) {
        Intent intent = new Intent(activity, PushHotissuesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    public static void launchCustomTextHotImageUI(Context context, HotIssue hotIssue) {
        Intent intent = new Intent(context, CustomTextHotImageActivity.class);
        intent.putExtra(CustomTextHotImageActivity.HOTISSUE, hotIssue);
        context.startActivity(intent);
    }
}
