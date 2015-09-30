package com.ekuater.admaker.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.PortfolioCommentVO;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.datastruct.SimpleUserVO;
import com.ekuater.admaker.delegate.AccountManager;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.PortfolioManager;
import com.ekuater.admaker.ui.holder.BaseViewHolder;
import com.ekuater.admaker.ui.holder.DragonBallFooter;
import com.ekuater.admaker.ui.holder.ItemListener;
import com.ekuater.admaker.ui.util.DateTimeUtils;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.ui.widget.CircleImageView;
import com.ekuater.admaker.ui.widget.ClickEventInterceptLinear;
import com.ekuater.admaker.util.BmpUtils;
import com.ekuater.admaker.util.TextUtil;
import com.karumi.headerrecyclerview.HeaderRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Administrator on 2015/7/4.
 */
public class CommunityDescriptionAdapter extends HeaderRecyclerViewAdapter<RecyclerView.ViewHolder, PortfolioVO, PortfolioCommentVO, DragonBallFooter> {

    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<PortfolioCommentVO> mArrayList;
    private ItemListener.AbsListener mItemListener;
    private AdElementDisplay mAdElementDisplay;
    private PortfolioVO mPortfolio = null;
    private DisplayMetrics mDisplayMetrics;
    public ImageView mImageView;

    public CommunityDescriptionAdapter(Context context, DisplayMetrics displayMetrics,ItemListener.AbsListener itemListener) {
        mContext = context;
        mArrayList = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        mItemListener = itemListener;
        mDisplayMetrics = displayMetrics;
        mAdElementDisplay = AdElementDisplay.getInstance(context);
    }

    public void addCommentVOs(PortfolioCommentVO[] commentVOs, int flags) {
        if (commentVOs != null && commentVOs.length > 0) {
            if (flags == CommunityDescriptionActivity.REFRESH) {
                mArrayList.clear();
            }
            Collections.addAll(mArrayList, commentVOs);
            setItems(mArrayList);
            notifyDataSetChanged();
        }
    }

    public void addCommentVOs(PortfolioCommentVO commentVOs) {
        mArrayList.add(commentVOs);
        setItems(mArrayList);
        notifyDataSetChanged();
    }

    @Override
    protected RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup, int i) {
        View rooView = mInflater.inflate(R.layout.description_recycler_header, viewGroup, false);
        return new CommunityHeaderViewHolder(rooView, mItemListener);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup viewGroup, int i) {
        View rootView = mInflater.inflate(R.layout.community_descrit_comment_item, viewGroup, false);
        return new CommunityDescriptViewHolder(rootView, mItemListener);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    protected void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int i) {
        int width = mDisplayMetrics.widthPixels - BmpUtils.dp2px(mContext, 10);
        CommunityHeaderViewHolder viewHolder = (CommunityHeaderViewHolder) holder;
        viewHolder.recender(getHeader(), width);
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder holder, int i) {
        CommunityDescriptViewHolder viewHolder = (CommunityDescriptViewHolder) holder;
        viewHolder.recender(getItem(i));
    }

    @Override
    protected void onBindFooterViewHolder(RecyclerView.ViewHolder holder, int i) {

    }

    private class CommunityDescriptViewHolder extends BaseViewHolder {

        private CircleImageView tx;
        private TextView name;
        private TextView time;
        private TextView comment;
        private TextView replyComment;

        public CommunityDescriptViewHolder(View itemView, ItemListener.AbsListener itemListener) {
            super(itemView, itemListener);
            tx = (CircleImageView) itemView.findViewById(R.id.comment_item_tx);
            name = (TextView) itemView.findViewById(R.id.comment_item_name);
            time = (TextView) itemView.findViewById(R.id.comment_item_time);
            comment = (TextView) itemView.findViewById(R.id.comment_item_comment);
            replyComment = (TextView) itemView.findViewById(R.id.comment_item_reply_comment);

            tx.setOnClickListener(this);
            itemView.findViewById(R.id.comment_item_card).setOnClickListener(this);
        }

        @Override
        protected void onRecyclerClick(View v) {
            switch (v.getId()){
                case R.id.comment_item_tx:
                    mItemListener.onHeaderAvatarImageClick(getPosition());
                    break;
                case R.id.comment_item_card:
                    mItemListener.onItemClick(null, getPosition());
                    break;
                default:
                    break;
            }
        }

        public void recender(PortfolioCommentVO commentVO) {
            if (commentVO != null) {
                SimpleUserVO userVO = commentVO.getUserVO();
                if (userVO != null) {
                    name.setText(commentVO.getUserVO().getNickname());
                    mAdElementDisplay.displayOnlineImage(commentVO.getUserVO().getAvatarThumb(), tx);
                }
                time.setText(DateTimeUtils.getTimeString(mContext, commentVO.getCreateDate()));
                comment.setText(commentVO.getComment());
                replyComment.setVisibility(TextUtil.isEmpty(commentVO.getReplyComment()) ? View.GONE : View.VISIBLE);
                if (!TextUtil.isEmpty(commentVO.getReplyComment())) {
                    replyComment.setText(mContext.getString(R.string.reply, commentVO.getReplyNickname()) + "“" + commentVO.getReplyComment() + "”");
                }
            }
        }
    }

    private class CommunityHeaderViewHolder extends BaseViewHolder {
        private ClickEventInterceptLinear mPraiseBtn;
        private ClickEventInterceptLinear mCommentBtn;
        private ClickEventInterceptLinear mShareBtn;
        private ImageView mPraiseImage;
        private TextView mPraiseNum;
        private TextView mCommentNum;
        private TextView mContentText;
        private TextView mNameText;
        private TextView mTimeText;
        private CircleImageView mUserTx;

        private boolean mIsPraise;

        public CommunityHeaderViewHolder(View itemView, ItemListener.AbsListener itemListener) {
            super(itemView, itemListener);
            mNameText = (TextView) itemView.findViewById(R.id.item_name);
            mTimeText = (TextView) itemView.findViewById(R.id.item_time);
            mContentText = (TextView) itemView.findViewById(R.id.item_content);
            mImageView = (ImageView) itemView.findViewById(R.id.item_image);
            mUserTx = (CircleImageView) itemView.findViewById(R.id.item_image_tx);
            mPraiseBtn = (ClickEventInterceptLinear) itemView.findViewById(R.id.operation_praise);
            mCommentBtn = (ClickEventInterceptLinear) itemView.findViewById(R.id.operation_comment);
            mShareBtn = (ClickEventInterceptLinear) itemView.findViewById(R.id.operation_share);
            mPraiseNum = (TextView) itemView.findViewById(R.id.operation_praise_num);
            mCommentNum = (TextView) itemView.findViewById(R.id.operation_comment_num);
            mPraiseImage = (ImageView) itemView.findViewById(R.id.operation_praise_image);

            itemView.setOnClickListener(this);
            mPraiseBtn.setOnClickListener(this);
            mCommentBtn.setOnClickListener(this);
            mUserTx.setOnClickListener(this);
            mImageView.setOnClickListener(this);
            mShareBtn.setOnClickListener(this);
        }

        public void recender(PortfolioVO portfolioVO, int width) {
            if (portfolioVO != null) {
                mImageView.setLayoutParams(new RelativeLayout.LayoutParams(width, (int) (width / 1.5)));
                mAdElementDisplay.loadPortfolioImage(portfolioVO, mContext, mImageView,width);
                mAdElementDisplay.displayOnlineImage(portfolioVO.getUserVO().getAvatarThumb(), mUserTx);
                mNameText.setText(portfolioVO.getUserVO().getNickname());
                mNameText.setTextSize(16);
                mTimeText.setTextSize(14);
//              mContentText.setText(portfolioVO.getContent());
//              mContentText.setVisibility(View.VISIBLE);
                mPraiseNum.setVisibility(portfolioVO.getPraiseNum() == 0 ? View.INVISIBLE:View.VISIBLE);
                mCommentNum.setVisibility(portfolioVO.getCommentNum() == 0 ? View.INVISIBLE:View.VISIBLE);
                mPraiseNum.setText(portfolioVO.getPraiseNum() + "");
                mCommentNum.setText(portfolioVO.getCommentNum() + "");
                mTimeText.setText(DateTimeUtils.getTimeString(mContext, portfolioVO.getCreateDate()).trim());
                mIsPraise = PortfolioManager.getInstance(mContext).isPortfolioPraised(portfolioVO.getPortfolioId());
                updateHeartButton(false);
            }
        }

        @Override
        protected void onRecyclerClick(View v) {
            switch (v.getId()) {
                case R.id.item_image_tx:
                    mItemListener.onAvatarImageClick();
                    break;
                case R.id.item_image:
                    mItemListener.onImageClick();
                    break;
                case R.id.operation_comment:
                    mItemListener.onCommentClick();
                    break;
                case R.id.operation_praise:
                    if (AccountManager.getInstance(mContext).isLogin()) {
                        if (!mIsPraise) {
                            updateHeartButton(true);
                            mItemListener.onPraiseClick(0,null);
                        }
                    } else {
                        ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.getString(R.string.login_prompt)).show();
                    }
                    break;
                case R.id.operation_share:
                    mItemListener.onShareClick();
                    break;
                default:
                    break;
            }
        }

        private void updateHeartButton(boolean animated) {
            if (animated) {
                AnimatorSet animatorSet = new AnimatorSet();
                ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(mPraiseImage, "rotation", 0f, 360f);
                rotationAnim.setDuration(300);
                rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

                ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(mPraiseImage, "scaleX", 0.2f, 1f);
                bounceAnimX.setDuration(300);
                bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

                ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(mPraiseImage, "scaleY", 0.2f, 1f);
                bounceAnimY.setDuration(300);
                bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
                bounceAnimY.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mPraiseImage.setImageResource(R.drawable.ic_like_pressed);
                    }
                });
                animatorSet.play(rotationAnim);
                animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }
                });
                animatorSet.start();
            } else {
                mPraiseImage.setImageResource(mIsPraise ? R.drawable.ic_like_pressed : R.drawable.ic_like_normal);
            }
        }
    }
}
