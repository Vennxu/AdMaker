package com.ekuater.admaker.ui.fragment.text;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ekuater.admaker.EnvConfig;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.Term;
import com.ekuater.admaker.datastruct.eventbus.FontEvent;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.fragment.AdStickerListener;
import com.ekuater.admaker.ui.fragment.AdvertiseAdapter;
import com.ekuater.admaker.ui.widget.AutoScaleTextView;
import com.ekuater.admaker.ui.widget.VerticalTextView;
import com.ekuater.admaker.util.BmpUtils;
import com.ekuater.admaker.util.L;
import com.ekuater.admaker.util.TextUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * Created by Administrator on 2015/6/1.
 *
 * @author Xu wenxiang
 */
public class FontTextFragment extends Fragment {

    private static final String TAG = FontTextFragment.class.getSimpleName();
    public static final String ARGS_AD_TERMEFFECT = "ARGS_AD_TERMEFFECT";
    public static final String ARGS_AD_TERMEFFECT_TYPE = "type";
    public static final String ARGS_AD_TERMEFFECT_TEXT = "simpleText";
    private int mSelected = 0;

    public int mType;
    public ArrayList<Term> terms;
    private String simpleText;

    public static FontTextFragment newInstance(ArrayList<Term> list, int type, String simpleText) {
        FontTextFragment instance = new FontTextFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARGS_AD_TERMEFFECT, list);
        args.putInt(ARGS_AD_TERMEFFECT_TYPE, type);
        args.putString(ARGS_AD_TERMEFFECT_TEXT, simpleText);
        instance.setArguments(args);
        return instance;
    }

    private AdStickerListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        terms = (args != null)
                ? args.<Term>getParcelableArrayList(ARGS_AD_TERMEFFECT) : null;
        mType = args.getInt(ARGS_AD_TERMEFFECT_TYPE);
        simpleText = args.getString(ARGS_AD_TERMEFFECT_TEXT);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (AdStickerListener) activity;
        } catch (ClassCastException e) {
            L.w(TAG, "onAttach(), class cast to listener failed.");
            listener = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.customtext_grid_view, container, false);
        FontTextAdapter fontTextAdapter = new FontTextAdapter(getActivity());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(fontTextAdapter);
        List<Object> objects = new ArrayList<>();
        objects.addAll(terms);
        fontTextAdapter.updateData(objects);
        return recyclerView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private class FontTextAdapter extends AdvertiseAdapter {
        private DisplayMetrics mDisplayMetrics;
        private EventBus mEventBus;

        public FontTextAdapter(Context context) {
            super(context);
            mDisplayMetrics = context.getResources().getDisplayMetrics();
            mEventBus = UIEventBusHub.getDefaultEventBus();
        }

        @Override
        protected int getLayout() {
            return R.layout.font_render_item;
        }

        @Override
        protected AdvertiseViewHolder initViews(ViewGroup parent, int viewType) {
            return new FontTextViewHolder(context, (inflater.inflate(R.layout.font_render_item, parent, false)));
        }

        public class FontTextViewHolder extends AdvertiseViewHolder {
            public VerticalTextView fondText;
            public RelativeLayout fondArea;

            public FontTextViewHolder(Context context, View itemView) {
                super(context, itemView);
                fondText = (VerticalTextView) itemView.findViewById(R.id.font_text);
                fondArea = (RelativeLayout) itemView.findViewById(R.id.font_area);
            }

            @Override
            protected void recender(Object object) {
                if (object instanceof Term) {
                    Term term = (Term) object;
                    Typeface customFont = null;
                    if (!TextUtil.isEmpty(term.getFont())) {
                        File file = new File(EnvConfig.genFontFile().getAbsolutePath() + "/" + term.getFont());
                        customFont = Typeface.createFromFile(file);
                    }
                    updateFontColor();
                    fondText.setTypeface(customFont);
                    fondText.setText(simpleText);
                    fondText.setTextColor(getResources().getColor(R.color.font_color_normal));
                    fondText.setOnClickListener(this);
                    setObject(term);
                }
            }

            @Override
            public void onClick(View v) {
                mEventBus.post(new FontEvent(((Term) object).getFont()));
                mSelected = getAdapterPosition();
                notifyDataSetChanged();
            }

            private void updateFontColor() {
                if (mSelected == getAdapterPosition()) {
                    fondArea.setBackgroundColor(getResources().getColor(R.color.actionBarStyle));
                } else {
                    fondArea.setBackgroundColor(Color.WHITE);
                }
            }
        }

    }

}
