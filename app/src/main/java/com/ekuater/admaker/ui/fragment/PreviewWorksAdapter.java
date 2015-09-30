package com.ekuater.admaker.ui.fragment;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.PortfolioManager;
import com.ekuater.admaker.ui.util.DateTimeUtils;
import com.ekuater.admaker.ui.widget.ClickEventInterceptLinear;
import com.ekuater.admaker.util.BmpUtils;

/**
 * Created by Leo on 2015/7/15.
 *
 * @author LinYong
 */
public class PreviewWorksAdapter extends BaseAdapter {

    private PortfolioVO[] mPortfolioVOs;
    private Context mContext;
    private int mWidth;
    private LayoutInflater mInflater;
    private AdElementDisplay mAdElementDisplay;
    private PortfolioManager mPortfolioManager;
    private PreviewWorksClickListener mListener;

    public PreviewWorksAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mPortfolioManager = PortfolioManager.getInstance(mContext);
        mAdElementDisplay = AdElementDisplay.getInstance(mContext);
    }

    public void setWidth(int width) {
        mWidth = width;
        notifyDataSetChanged();
    }

    public void updatePortfolios(PortfolioVO[] portfolioVOs) {
        mPortfolioVOs = portfolioVOs;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPortfolioVOs != null ? mPortfolioVOs.length : 0;
    }

    @Override
    public PortfolioVO getItem(int position) {
        return mPortfolioVOs != null ? mPortfolioVOs[position] : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(parent);
        }
        bindView(position, convertView);
        return convertView;
    }

    private View newView(ViewGroup parent) {
        HotissuesViewHolder holder = new HotissuesViewHolder();
        View view = mInflater.inflate(R.layout.item_main_hot_image, parent, false);
        holder.cardView = (CardView) view.findViewById(R.id.hot_child_relayout);
        holder.imageView = (ImageView) view.findViewById(R.id.hot_child_image);
        holder.praise = (ClickEventInterceptLinear) view.findViewById(R.id.image_praise);
        holder.praiseNum = (TextView) view.findViewById(R.id.image_praise_text);
        holder.praiseImage = (ImageView) view.findViewById(R.id.image_praise_image);
        ViewGroup.LayoutParams layoutParams = holder.cardView.getLayoutParams();

        int width = (mWidth - BmpUtils.dp2px(mContext, 10)) / 2;
        layoutParams.width = width;
        layoutParams.height = (int) (width / 1.5);
        view.setTag(holder);
        return view;
    }

    private void bindView(final int position, View view) {
        HotissuesViewHolder holder = (HotissuesViewHolder) view.getTag();
        if (mPortfolioVOs != null) {
            final PortfolioVO portfolioVO = getItem(position);
            if (portfolioVO != null) {
                mAdElementDisplay.displayPortfolioThumbImage(portfolioVO, holder.imageView);
                holder.praiseNum.setText(portfolioVO.getPraiseNum() < 1 ? "" : portfolioVO.getPraiseNum() + " ");
                holder.praiseImage.setImageResource(mPortfolioManager.isPortfolioPraised(portfolioVO.getPortfolioId()) ? R.drawable.ic_nice_selected : R.drawable.ic_nice_normal);
                holder.praise.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onPraiseClick(portfolioVO, position);
                        }
                    }
                });
            }
        }
    }

    private String getTimeString(long time) {
        return DateTimeUtils.getTimeString(mContext, time).trim();
    }

    public void setPreviewWorksClickListener(PreviewWorksClickListener listener){
        mListener = listener;
    }

    public interface PreviewWorksClickListener {
        void onPraiseClick(PortfolioVO portfolioVO, int position);
    }

    private class HotissuesViewHolder {
        private CardView cardView;
        private ImageView imageView;
        private TextView praiseNum;
        private ImageView praiseImage;
        private ClickEventInterceptLinear praise;
    }
}

