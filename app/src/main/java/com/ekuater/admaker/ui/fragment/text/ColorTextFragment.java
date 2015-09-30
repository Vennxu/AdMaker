package com.ekuater.admaker.ui.fragment.text;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.eventbus.ColorEvent;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.UILauncher;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;

import de.greenrobot.event.EventBus;


/**
 * Created by Administrator on 2015/6/1.
 *
 * @author Xu wenxiang
 */
public class ColorTextFragment extends Fragment {

    private ColorPickerView colorPickerView;
    private EventBus eventBus;
    private View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventBus = UIEventBusHub.getDefaultEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_text_color, container, false);
            colorPickerView = (ColorPickerView) view.findViewById(R.id.color_picker_view);
            colorPickerView.addOnColorSelectedListener(new OnColorSelectedListener() {
                @Override
                public void onColorSelected(int selectedColor) {
                    eventBus.post(new ColorEvent(selectedColor));
                }
            });
        }
        return view;
    }
}

