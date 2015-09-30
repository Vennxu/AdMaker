package com.ekuater.admaker.ui.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.admaker.R;

/**
 * @author LinYong
 */
public class ConfirmDialogFragment extends DialogFragment {

    public static final class UiConfig {

        public final boolean mShowTitle;
        public final CharSequence mTitle;
        public final Drawable mIcon;
        public final CharSequence mMessage;
        public final CharSequence mSubMessage;
        public final boolean mOKPrefer;

        public UiConfig(boolean showTitle, CharSequence title, Drawable icon,
                        CharSequence message, CharSequence subMessage, boolean okPrefer) {
            mShowTitle = showTitle;
            mTitle = title;
            mIcon = icon;
            mMessage = message;
            mSubMessage = subMessage;
            mOKPrefer = okPrefer;
        }

        public UiConfig(boolean showTitle, CharSequence title, Drawable icon,
                        CharSequence message, CharSequence subMessage) {
            this(showTitle, title, icon, message, subMessage, true);
        }

        public UiConfig(CharSequence message, CharSequence subMessage, boolean okPrefer) {
            this(false, null, null, message, subMessage, okPrefer);
        }

        public UiConfig(CharSequence message, CharSequence subMessage) {
            this(message, subMessage, true);
        }
    }

    public interface IConfirmListener {

        public void onCancel();

        public void onConfirm();
    }

    public static class AbsConfirmListener implements IConfirmListener {

        @Override
        public void onCancel() {
        }

        @Override
        public void onConfirm() {
        }
    }

    public static ConfirmDialogFragment newInstance(UiConfig uiConfig,
                                                    IConfirmListener listener) {
        ConfirmDialogFragment instance = new ConfirmDialogFragment();
        instance.initialize(uiConfig, listener);
        return instance;
    }

    private UiConfig mUiConfig;
    private IConfirmListener mConfirmListener;
    private final View.OnClickListener mButtonHandler = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            dismiss();

            switch (v.getId()) {
                case R.id.btn_cancel:
                    if (mConfirmListener != null) {
                        mConfirmListener.onCancel();
                    }
                    break;
                case R.id.btn_ok:
                    if (mConfirmListener != null) {
                        mConfirmListener.onConfirm();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public ConfirmDialogFragment() {
    }

    public void initialize(UiConfig uiConfig, IConfirmListener confirmer) {
        mUiConfig = uiConfig;
        mConfirmListener = confirmer;
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.confirm_dialog_layout, container, false);

        final boolean showTitle = mUiConfig.mShowTitle;
        final Drawable icon = mUiConfig.mIcon;
        final CharSequence title = mUiConfig.mTitle;
        final CharSequence message = mUiConfig.mMessage;
        final CharSequence subMessage = mUiConfig.mSubMessage;

        if (showTitle) {
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);
            TextView titleView = (TextView) view.findViewById(R.id.title);

            iconView.setImageDrawable(icon);
            titleView.setText(title);
        } else {
            view.findViewById(R.id.titlePanel).setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(message) || !TextUtils.isEmpty(subMessage)) {
            view.findViewById(R.id.contentPanel).setVisibility(View.VISIBLE);

            if (!TextUtils.isEmpty(message)) {
                TextView messageView = (TextView) view.findViewById(R.id.message);
                messageView.setVisibility(View.VISIBLE);
                messageView.setText(message);
                messageView.setTextColor(getResources().getColor(R.color.login_dialog_color));
            }
            if (!TextUtils.isEmpty(subMessage)) {
                TextView subMessageView = (TextView) view.findViewById(R.id.sub_message);
                subMessageView.setVisibility(View.VISIBLE);
                subMessageView.setText(subMessage);
            }
        }

        TextView cancelBtn = (TextView) view.findViewById(R.id.btn_cancel);
        TextView okBtn = (TextView) view.findViewById(R.id.btn_ok);
        cancelBtn.setOnClickListener(mButtonHandler);
        okBtn.setOnClickListener(mButtonHandler);
        final int preferColor = getResources().getColor(R.color.colorPrimary);
        if (mUiConfig.mOKPrefer) {
            okBtn.setTextColor(preferColor);
        } else {
            cancelBtn.setTextColor(preferColor);
        }

        return view;
    }
}
