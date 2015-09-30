package com.ekuater.admaker.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.datastruct.eventbus.AddStickerDoneEvent;
import com.ekuater.admaker.delegate.AdStickerManager;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.fragment.SimpleProgressHelper;
import com.ekuater.admaker.ui.util.BitmapUtils;


import cropper.CropImageView;
import cropper.CropSquareImageView;
import de.greenrobot.event.EventBus;


/**
 * Created by Administrator on 2015/6/18.
 */
public class CuterImageActivity extends BackIconActivity implements View.OnClickListener {

    public static final String CUTER_URI = "cuter_uri";
    private static final int DEFAULT_ASPECT_RATIO_VALUES = 10;
    private static final int ROTATE_NINETY_DEGREES = 90;
    private String uri;
    private boolean isCircle = false;
    private Bitmap circleBitmap = null;

    private CropImageView cropImageView;
    private CropSquareImageView cropSquareImageView;
    private SimpleProgressHelper mProgressHelper;
    private DisplayMetrics mMetrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuter_image);
        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        mProgressHelper = new SimpleProgressHelper(this);
        uri = getIntent().getStringExtra(CUTER_URI);
        cropImageView = (CropImageView) findViewById(R.id.crop_circle_image_view);
        cropSquareImageView = (CropSquareImageView) findViewById(R.id.crop_square_image_view);
        cropImageView.setFixedAspectRatio(true);

        cropImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES, DEFAULT_ASPECT_RATIO_VALUES);
        cropSquareImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES, DEFAULT_ASPECT_RATIO_VALUES);
        findViewById(R.id.cuter_circle).setOnClickListener(this);
        findViewById(R.id.cuter_revolve).setOnClickListener(this);
        findViewById(R.id.cuter_square).setOnClickListener(this);
        getCropBitmap();
        cropSquareImageView.setImageBitmap(circleBitmap);
        initTitle();
    }

    private void initTitle(){
        TextView title = (TextView) findViewById(R.id.title);
        TextView rightTitle = (TextView) findViewById(R.id.right_title);
        ImageView icon = (ImageView) findViewById(R.id.icon);
        title.setText(getString(R.string.custom_trademark));
        icon.setOnClickListener(this);
        rightTitle.setVisibility(View.VISIBLE);
        rightTitle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cuter_revolve:
                if (isCircle) {
                    cropImageView.rotateImage(ROTATE_NINETY_DEGREES);
                } else {
                    cropSquareImageView.rotateImage(ROTATE_NINETY_DEGREES);
                }
                break;
            case R.id.right_title:
                cropImageView.setVisibility(View.GONE);
                mProgressHelper.show();
                Bitmap luminousBitmap = null;
                try {
                    luminousBitmap = isCircle ? getLuminousCircle() : getLuminousBitmap();
                } catch (OutOfMemoryError outOfMemoryError) {
                    System.gc();
                    return;
                }
                AdSticker adSticker = AdStickerManager.getInstance(this).addNewCustomAdSticker(null, AdSticker.Type.TRADEMARK, luminousBitmap, luminousBitmap);
                if (adSticker != null) {
                    Intent intent = new Intent();
                    intent.putExtra(CustomTextActivity.CUSTOM_TEXT, adSticker);
                    setResult(RESULT_OK, intent);
                    EventBus.getDefault().post(new AddStickerDoneEvent());
                    finish();
                }
                break;
            case R.id.cuter_square:
                cropImageView.setVisibility(View.GONE);
                cropSquareImageView.setVisibility(View.VISIBLE);
                isCircle = false;
                getCropBitmap();
                cropSquareImageView.setImageBitmap(circleBitmap);
                break;
            case R.id.cuter_circle:
                cropImageView.setVisibility(View.VISIBLE);
                cropSquareImageView.setVisibility(View.GONE);
                isCircle = true;
                getCropBitmap();
                cropImageView.setImageBitmap(circleBitmap);
                break;
            case R.id.icon:
                finish();
                break;
        }
    }

    private void getCropBitmap() {
        if (circleBitmap == null) {
            try {
                circleBitmap = BitmapUtils.getBitmapFromUri(uri, mMetrics.widthPixels);
            } catch (OutOfMemoryError outOfMemoryError) {
                System.gc();
                circleBitmap = null;
                return;
            }
        }
    }

    private Bitmap getLuminousBitmap() {
        Bitmap bitmap = cropSquareImageView.getCroppedImage();
        NinePatchDrawable drawable = (NinePatchDrawable) getResources().getDrawable(R.drawable.ic_custom_logo_luminous);
        drawable.setBounds(0, 0, bitmap.getWidth() + 14, bitmap.getHeight() + 14);
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap.getWidth() + 14, bitmap.getHeight() + 14, bitmap.getConfig());
        Canvas canvas = new Canvas(bitmap1);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect rectF = new Rect(14, 14, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(bitmap, rect, rectF, new Paint());
        drawable.draw(canvas);
        return bitmap1;
    }

    private Bitmap getLuminousCircle() {
        Bitmap bitmap = BitmapUtils.resizeImage(cropImageView.getCroppedCircleImage(), 200, 200);
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_custom_logo_luminous_circle);
        Bitmap bitmap2 = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), bitmap1.getConfig());
        Canvas canvas = new Canvas(bitmap2);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect rectF = new Rect(14, 14, bitmap.getWidth() + 14, bitmap.getHeight() + 14);
        canvas.drawBitmap(bitmap, rect, rectF, new Paint());
        canvas.drawBitmap(bitmap1, new Matrix(), new Paint());
        return bitmap2;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (circleBitmap != null) {
            circleBitmap.recycle();
            System.gc();
        }
    }
}
