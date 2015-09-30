package com.ekuater.admaker.ui.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ekuater.admaker.R;

/**
 * Created by Administrator on 2015/3/30.
 *
 * @author FanChong
 */
public class ShowToast {

    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;

    public static Toast makeText(Context context, int iconId, CharSequence text) {
        return makeText(context, iconId, text, LENGTH_SHORT);
    }

    public static Toast makeText(Context context, int iconId, CharSequence text, int duration) {
        Toast toast = new Toast(context);
        @SuppressLint("InflateParams")
        View layout = LayoutInflater.from(context).inflate(R.layout.my_toast, null);
        ImageView imageView = (ImageView) layout.findViewById(R.id.toast_image);
        TextView textView = (TextView) layout.findViewById(R.id.toast_content);

        imageView.setImageResource(iconId);
        textView.setText(text);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(duration);
        return toast;
    }
}
