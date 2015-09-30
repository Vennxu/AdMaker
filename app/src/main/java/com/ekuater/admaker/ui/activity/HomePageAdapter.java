package com.ekuater.admaker.ui.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.datastruct.SimpleUserVO;
import com.ekuater.admaker.datastruct.UserVO;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.PortfolioManager;
import com.ekuater.admaker.ui.fragment.SimpleProgressHelper;
import com.ekuater.admaker.ui.holder.BaseViewHolder;
import com.ekuater.admaker.ui.holder.ItemListener;
import com.ekuater.admaker.ui.util.BitmapUtils;
import com.ekuater.admaker.ui.util.DateTimeUtils;
import com.ekuater.admaker.ui.util.ScreenUtils;
import com.ekuater.admaker.ui.widget.CircleImageView;
import com.ekuater.admaker.ui.widget.ClickEventInterceptLinear;
import com.ekuater.admaker.ui.widget.RoundImageView;
import com.ekuater.admaker.util.BmpUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Administrator on 2015/7/7.
 */
public class HomePageAdapter extends RecyclerView.Adapter {

    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_NORMAL = 1;

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<PortfolioVO> mPortfolioVOs;
    private AdElementDisplay mAdElementDisplay;
    private ItemListener.AbsListener mItemListener;
    private SimpleUserVO mUserVo;
    private PortfolioManager mPortfolioManager;

    public HomePageAdapter(Context context, SimpleUserVO userVO, ItemListener.AbsListener itemListener) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mPortfolioVOs = new ArrayList<>();
        mItemListener = itemListener;
        mAdElementDisplay = AdElementDisplay.getInstance(mContext);
        mPortfolioManager = PortfolioManager.getInstance(mContext);
        mUserVo = userVO;
    }

    public void addPortfolioVOs(PortfolioVO[] portfolioVOs) {
        if (portfolioVOs != null && portfolioVOs.length > 0) {
            Collections.addAll(mPortfolioVOs, portfolioVOs);
            notifyDataSetChanged();
        }
    }

    public PortfolioVO getItem(int position) {
        return mPortfolioVOs.get(position);
    }

    public void removeItem(int position) {
        mPortfolioVOs.remove(position);
        notifyDataSetChanged();
    }

    public int getImageWidth() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        return (windowManager.getDefaultDisplay().getWidth() - BmpUtils.dp2px(mContext, 12)) / 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_HEADER) {
            View headerView = mInflater.inflate(R.layout.home_page_header, parent, false);
            return new HomePageHeaderViewHolder(headerView, mItemListener);
        } else {
            View normalView = mInflater.inflate(R.layout.home_page_item, parent, false);
            return new HomePageNormalViewHolder(normalView, getImageWidth(), mItemListener);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isHeader(position)) {
            HomePageHeaderViewHolder viewHolder = (HomePageHeaderViewHolder) holder;
            viewHolder.recender(mUserVo);
        } else {
            HomePageNormalViewHolder viewHolder = (HomePageNormalViewHolder) holder;
            viewHolder.recender(getItem(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return mPortfolioVOs == null ? 1 : mPortfolioVOs.size() + 1;
    }

    public boolean isHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_NORMAL;
    }

    public class HomePageHeaderViewHolder extends BaseViewHolder {

        private CircleImageView mUserAvatar;
        private TextView mUserComment;
        private TextView mUserPraise;
        private TextView mUserName;

        public HomePageHeaderViewHolder(View itemView, ItemListener.AbsListener itemListener) {
            super(itemView, itemListener);
            mUserAvatar = (CircleImageView) itemView.findViewById(R.id.header_tx);
            mUserComment = (TextView) itemView.findViewById(R.id.header_comment);
            mUserPraise = (TextView) itemView.findViewById(R.id.header_praise);
            mUserName = (TextView) itemView.findViewById(R.id.header_name);
        }

        public void recender(SimpleUserVO userVO) {
            if (userVO != null) {
                Drawable drawable = mContext.getResources().getDrawable(ConstantCode.getSexImageResource(userVO.getGender()));
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mUserName.setCompoundDrawables(null, null, drawable, null);
                mUserName.setText(userVO.getNickname());
                mAdElementDisplay.displayOnlineImage(userVO.getAvatarThumb(), mUserAvatar);
            }
        }

        @Override
        protected void onRecyclerClick(View v) {

        }
    }

    public class HomePageNormalViewHolder extends BaseViewHolder {

        private CardView cardView;
        private ImageView imageView;
        private TextView praiseNum;
        private ImageView praiseImage;
        private ClickEventInterceptLinear praise;
        private boolean isLongClickable;

        public HomePageNormalViewHolder(View itemView, int width, ItemListener.AbsListener itemListener) {
            super(itemView, itemListener);
            cardView = (CardView) itemView.findViewById(R.id.hot_child_relayout);
            imageView = (ImageView) itemView.findViewById(R.id.hot_child_image);
            praise = (ClickEventInterceptLinear) itemView.findViewById(R.id.image_praise);
            praiseNum = (TextView) itemView.findViewById(R.id.image_praise_text);
            praiseImage = (ImageView) itemView.findViewById(R.id.image_praise_image);
            ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
            layoutParams.height = width;
            layoutParams.height = (int) (width / 1.5);
            itemView.setOnClickListener(this);
            ((RippleView) itemView.findViewById(R.id.homa_page_rippleview)).setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                @Override
                public void onComplete(RippleView rippleView) {
                    rippleView.setLongClickable(isLongClickable);
                    if (rippleView.isLongClickable()) {
                        isLongClickable = false;
                        mItemListener.onDeleteItemClick(null, getAdapterPosition() - 1);
                    } else {
                        isLongClickable = true;
                        mItemListener.onItemClick(null, getAdapterPosition() - 1);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    isLongClickable = true;
                    return false;
                }
            });
        }

        public void recender(final PortfolioVO portfolioVO) {
            if (portfolioVO != null) {
                AdElementDisplay.getInstance(mContext).displayPortfolioImage(portfolioVO, imageView);
                praiseNum.setText(portfolioVO.getPraiseNum() < 1 ? "" : portfolioVO.getPraiseNum() + " ");
                praiseImage.setImageResource(mPortfolioManager.isPortfolioPraised(portfolioVO.getPortfolioId()) ? R.drawable.ic_nice_selected : R.drawable.ic_nice_normal);
                praise.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mItemListener != null) {
                            mItemListener.onPraiseClick(getAdapterPosition() - 1, portfolioVO);
                        }
                    }
                });
            }
        }

        @Override
        protected void onRecyclerClick(View v) {
            isLongClickable = false;
        }
    }
}
