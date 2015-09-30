package com.ekuater.admaker.ui.fragment.image;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.ekuater.admaker.R;
import com.ekuater.admaker.ui.util.ShowToast;

/**
 * Created by Administrator on 2015/3/27.
 *
 * @author Xu Wenxinag
 */
public class KeepPhotoDialog extends AlertDialog {

    private Bitmap mBitmap;
    private Context context;

    public KeepPhotoDialog(Context context, int theme, Bitmap bitmap) {
        super(context, theme);
        mBitmap = bitmap;
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_keep_photo);
        findViewById(R.id.text_keep).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), mBitmap, null, null);
                ShowToast.makeText(context, R.drawable.emoji_smile, context.getResources().getString(R.string.saved) + path).show();
                dismiss();
            }
        });
    }


}
