package com.ekuater.admaker.ui.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.eventbus.FinishEvent;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.MiscManager;
import com.ekuater.admaker.delegate.NormalCallListener;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.fragment.ConfirmDialogFragment;
import com.ekuater.admaker.ui.fragment.SimpleProgressHelper;
import com.ekuater.admaker.ui.util.BitmapUtils;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.ui.widget.KeyboardStateView;
import com.ekuater.admaker.util.BmpUtils;
import com.ekuater.admaker.util.PhotoSaver;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * @author Leo
 */
public class FeedbackActivity extends BackIconActivity implements Handler.Callback {

    private static final int MSG_HANDLE_FEEDBACK_RESULT = 101;

    private EditText mMessageEdit;
    private EditText mContactEdit;
    private Button mSubmitBtn;
    private ImageView mImageView;
    private SimpleProgressHelper mProgressHelper;
    private KeyboardStateView mKeyboard;
    private TextView mMessageHint;
    private InputMethodManager inputManager;
    private Handler mHandler = new Handler(this);
    private final TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mSubmitBtn.setEnabled(!TextUtils.isEmpty(s.toString()));
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mProgressHelper = new SimpleProgressHelper(this);
        initView();
        setListener();
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_HANDLE_FEEDBACK_RESULT:
                handleFeedbackResult(msg.arg1 != 0);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void initView() {
        ImageView icon = (ImageView) findViewById(R.id.icon);
        TextView title = (TextView) findViewById(R.id.title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title.setText(R.string.feedback);
        mSubmitBtn = (Button) findViewById(R.id.btn_feedback);
        mMessageEdit = (EditText) findViewById(R.id.feedback_message);
        mMessageEdit.addTextChangedListener(mTextWatcher);
        mContactEdit = (EditText) findViewById(R.id.contact_info);
        mImageView = (ImageView) findViewById(R.id.feedback_image);
        mMessageHint = (TextView) findViewById(R.id.feedback_message_hint);
        mKeyboard = (KeyboardStateView) findViewById(R.id.input_keyboard);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels - (BmpUtils.dp2px(this, 20));
        AdElementDisplay.getInstance(this).displayFixedOnlineImage("http://www.ekuater.com/wx_feedback_group_qrcode.png", mImageView, this, width);
        mSubmitBtn.setEnabled(!TextUtils.isEmpty(mMessageEdit.getText().toString()));
        mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialog();
                return true;
            }
        });
        mMessageHint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessageHint.setVisibility(View.INVISIBLE);
                mMessageEdit.setVisibility(View.VISIBLE);
                mMessageEdit.requestFocus();
                inputManager.showSoftInput(mMessageEdit, 0);
            }
        });
        mMessageHint.setText(getString(R.string.feed_message));
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener = new ConfirmDialogFragment.AbsConfirmListener() {
        @Override
        public void onConfirm() {
            saveBitmap();
        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

    };

    private void showDialog() {
        ConfirmDialogFragment.UiConfig uiConfig = new ConfirmDialogFragment.UiConfig(getString(R.string.is_save_image_message), null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getSupportFragmentManager(), "ConfirmDialogFragment");
    }

    private void saveBitmap() {
        Bitmap sceneBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        if (sceneBitmap != null) {
            PhotoSaver.savePhoto(this, sceneBitmap, new PhotoSaver.OnSaveListener() {
                @Override
                public void onSaveCompleted(final String path) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(R.drawable.emoji_smile, getString(R.string.saved) + path);
                        }
                    });
                }
            });
        }
    }

    private void uploadFeedbackMessage() {
        final String message = mMessageEdit.getText().toString();
        final String contactInfo = mContactEdit.getText().toString();

        if (!TextUtils.isEmpty(message)) {
            MiscManager.getInstance(this).feedback(message, contactInfo,
                    new NormalCallListener() {
                        @Override
                        public void onCallResult(boolean success) {
                            mHandler.obtainMessage(MSG_HANDLE_FEEDBACK_RESULT,
                                    success ? 1 : 0, 0).sendToTarget();
                        }
                    });
            showProgressDialog();
        } else {
            showToast(R.drawable.emoji_sad, R.string.feedback_message_empty);
        }
    }

    private void handleFeedbackResult(boolean success) {
        dismissProgressDialog();
        if (success) {
            showToast(R.drawable.emoji_smile, R.string.feedback_success_prompt);
            finish();
        } else {
            showToast(R.drawable.emoji_sad, R.string.feedback_submit_failure);
        }
    }

    private void showToast(@DrawableRes int iconId, @StringRes int stringId) {
        ShowToast.makeText(this, iconId, getString(stringId)).show();
    }

    private void showToast(@DrawableRes int iconId, @StringRes String stringId) {
        ShowToast.makeText(this, iconId, stringId).show();
    }

    private void setListener() {
        mSubmitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFeedbackMessage();
            }
        });
    }

    private void showProgressDialog() {
        mProgressHelper.show();
    }

    private void dismissProgressDialog() {
        mProgressHelper.dismiss();
    }
}
