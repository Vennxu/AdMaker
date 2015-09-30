package com.ekuater.admaker.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.eventbus.CustomTextEvent;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.widget.KeyboardStateView;
import com.ekuater.admaker.util.TextUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/6/11.
 */
public class InputCustomTextActivity extends BackIconActivity {

    public static final String CUSTOM_TEXT = "text";

    private EditText editText;
    private EventBus mUIEventBus;
    private KeyboardStateView mKeyboard;
    private String mText;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mUIEventBus.post(new CustomTextEvent(s.toString()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_custom_text);
        Intent intent = getIntent();
        if (intent != null){
            mText = intent.getStringExtra(CUSTOM_TEXT);
        }
        mUIEventBus = UIEventBusHub.getDefaultEventBus();
        editText = (EditText) findViewById(R.id.input_edit);
        mKeyboard = (KeyboardStateView) findViewById(R.id.input_keyboard);
        editText.addTextChangedListener(textWatcher);
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        findViewById(R.id.input_frame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        });
        mKeyboard.setOnKeyboardStateChangedListener(new KeyboardStateView.OnKeyboardStateChangedListener() {
            @Override
            public void onKeyboardStateChanged(int state) {
                switch (state){
                    case KeyboardStateView.KEYBOARD_STATE_HIDE:
                        finish();
                        break;
                    case KeyboardStateView.KEYBOARD_STATE_SHOW:
                        break;
                }
            }
        });
        if (!TextUtil.isEmpty(mText)){
            editText.setText(mText);
            editText.setSelection(mText.length());
        }
    }
}
