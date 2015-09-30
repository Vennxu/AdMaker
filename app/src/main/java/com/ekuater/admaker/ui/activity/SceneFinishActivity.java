package com.ekuater.admaker.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.admaker.EnvConfig;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.Scene;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.util.BitmapUtils;

/**
 * Created by Administrator on 2015/6/3.
 */
public class SceneFinishActivity extends BackIconActivity {

    private ImageView mChooseImage;
    private Bitmap mBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        argmentParam();
        initView();
        initDate();
    }

    private void argmentParam(){
        Intent intent = getIntent();
        String url = intent.getStringExtra(AdvertiseActivity.EXTRA_OUTPUT_PATH);
        Scene scene = intent.getParcelableExtra(AdvertiseActivity.EXTRA_OUTPUT_SCENE);
        mBitmap = BitmapUtils.getMatrixBitmap(this, url, scene);
    }

    protected void initView(){
        TextView title = (TextView) findViewById(R.id.title);
        ImageView icon = (ImageView) findViewById(R.id.icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mChooseImage = (ImageView) findViewById(R.id.choose_image);
    }

    protected void initDate(){
        if (mBitmap != null) {
            mChooseImage.setImageBitmap(mBitmap);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap != null){
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}
