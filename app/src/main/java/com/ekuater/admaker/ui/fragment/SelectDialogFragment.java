package com.ekuater.admaker.ui.fragment;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ekuater.admaker.R;
import com.ekuater.admaker.ui.widget.DrawableCenterTextView;

/**
 * Created by Administrator on 2015/6/4.
 * @author Xu wenxiang
 */
public abstract class SelectDialogFragment extends DialogFragment implements View.OnClickListener{


    private int leftTopDrawable;
    private int rightTopDrawable;
    private String leftTopText;
    private String rightTopText;

    public SelectDialogFragment(){

    }

    public SelectDialogFragment(int leftTopDrawable, int rightTopDrawable, String leftTopText, String rightTopText){
        this.leftTopDrawable = leftTopDrawable;
        this.rightTopDrawable = rightTopDrawable;
        this.leftTopText = leftTopText;
        this.rightTopText = rightTopText;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCancelable(true);
        setStyle(STYLE_NO_TITLE,R.style.ShareDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.share_dialog, container, false);
        DrawableCenterTextView leftTopImage = (DrawableCenterTextView) rootView.findViewById(R.id.share_friend);
        DrawableCenterTextView rightTopImage = (DrawableCenterTextView) rootView.findViewById(R.id.share_cirle);
        leftTopImage.setCompoundDrawables(null,getDrawableTop(leftTopDrawable), null, null);
        rightTopImage.setCompoundDrawables(null, getDrawableTop(rightTopDrawable), null, null);
        leftTopImage.setText(leftTopText);
        rightTopImage.setText(rightTopText);
        rightTopImage.setOnClickListener(this);
        leftTopImage.setOnClickListener(this);
        return rootView;
    }

    private Drawable getDrawableTop(int drawable){
        Drawable drawableTop = getResources().getDrawable(drawable);
        drawableTop.setBounds(0,0,drawableTop.getMinimumWidth(), drawableTop.getMinimumHeight());
        return drawableTop;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.share_friend:
                onFistClick();
                dismiss();
                break;
            case R.id.share_cirle:
                onTwoClick();
                dismiss();
                break;
            default:
                break;
        }
    }

    protected abstract void onFistClick();

    protected abstract void onTwoClick();

}
