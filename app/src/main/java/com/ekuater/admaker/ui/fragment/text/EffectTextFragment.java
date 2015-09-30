package com.ekuater.admaker.ui.fragment.text;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.ekuater.admaker.R;

/**
 * Created by Administrator on 2015/6/11.
 */
public class EffectTextFragment extends Fragment implements RadioGroup.OnCheckedChangeListener{

    private EffectListener effectListener;
    private RadioGroup mEffectGroup, mEffectAlign, mEffectSelectColor, mEffectOrientation;

    public static EffectTextFragment newInstance(EffectListener effectListener) {
        EffectTextFragment instance = new EffectTextFragment();
        instance.effectListener = effectListener;
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_effect, container, false);
        mEffectGroup = (RadioGroup) view.findViewById(R.id.effect_group);
        mEffectAlign = (RadioGroup) view.findViewById(R.id.effect_align);
        mEffectSelectColor = (RadioGroup) view.findViewById(R.id.effect_select_color);
        mEffectOrientation = (RadioGroup) view.findViewById(R.id.effect_select_orientation);

        mEffectGroup.setOnCheckedChangeListener(this);
        mEffectAlign.setOnCheckedChangeListener(this);
        mEffectSelectColor.setOnCheckedChangeListener(this);
        mEffectOrientation.setOnCheckedChangeListener(this);
        return view;
    }

    private void onStickerPageCheckedChanged(int checkedId) {
        switch (checkedId) {
            case R.id.effect_left_align:
                effectListener.onRadioLeftAlign();
                break;
            case R.id.effect_center_align:
                effectListener.onRadioCenterAlign();
                break;
            case R.id.effect_right_align:
                effectListener.onRadioRightAlign();
                break;
            case R.id.effect_no_lumines:
                effectListener.onRadioNormal();
                break;
            case R.id.effect_lumines:
                effectListener.onRadioLuminess();
                break;
            case R.id.effect_stroke:
                effectListener.onRadioStroke();
                break;
            case R.id.effect_white_color:
                effectListener.onRadioWhiteColor();
                break;
            case R.id.effect_black_color:
                effectListener.onRadioBlackColor();
                break;
            case R.id.effect_horizontal:
                effectListener.onRadioHorizontal();
                break;
            case R.id.effect_vertical:
                effectListener.onRadioVertical();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        onStickerPageCheckedChanged(checkedId);
    }
}
