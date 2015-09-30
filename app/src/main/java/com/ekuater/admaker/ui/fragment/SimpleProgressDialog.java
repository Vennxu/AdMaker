package com.ekuater.admaker.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekuater.admaker.R;

/**
 * @author LinYong
 */
public class SimpleProgressDialog extends DialogFragment {

    private static final String EXTRA_TEXT = "extra_text";

    private String mText;

    public static SimpleProgressDialog newInstance() {
        return newInstance(null);
    }

    public static SimpleProgressDialog newInstance(String text) {
        SimpleProgressDialog instance = new SimpleProgressDialog();
        Bundle args = new Bundle();
        args.putString(EXTRA_TEXT, text);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
        Bundle args = getArguments();
        mText = args != null ? args.getString(EXTRA_TEXT) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_progress_dialog, container, false);
        TextView textView = (TextView) view.findViewById(R.id.progress_text);
        if (!TextUtils.isEmpty(mText)) {
            textView.setText(mText);
            textView.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void dismiss() {
        dismissAllowingStateLoss();
    }
}