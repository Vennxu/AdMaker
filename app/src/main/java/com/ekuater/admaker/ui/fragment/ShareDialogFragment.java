package com.ekuater.admaker.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ekuater.admaker.R;

/**
 * Created by Leo on 2015/8/4.
 *
 * @author Leo
 */
public abstract class ShareDialogFragment extends DialogFragment
        implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCancelable(true);
        setStyle(STYLE_NO_TITLE, R.style.ShareDialog);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.share_dialog, container, false);
        view.findViewById(R.id.share_friend).setOnClickListener(this);
        view.findViewById(R.id.share_cirle).setOnClickListener(this);
        view.findViewById(R.id.share_qq_friend).setOnClickListener(this);
        view.findViewById(R.id.share_xina).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
    }
}
