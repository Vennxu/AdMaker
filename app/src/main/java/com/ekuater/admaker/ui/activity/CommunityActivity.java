package com.ekuater.admaker.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.PortfolioLoadListener;
import com.ekuater.admaker.delegate.PortfolioManager;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.ui.widget.CircleImageView;
import com.ekuater.admaker.ui.widget.DrawableCircleImageView;
import com.ekuater.admaker.ui.widget.MaterialProgressDrawable;
import com.ekuater.admaker.ui.widget.SwipeRefreshLoadLayout;
import com.ekuater.admaker.util.BmpUtils;

import java.util.ArrayList;
import java.util.Collections;

import static com.ekuater.admaker.R.id.community_relayout;

/**
 * Created by Administrator on 2015/7/2.
 */
public class CommunityActivity extends BackIconActivity {


    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    private static final int DEFAULT_CIRCLE_TARGET = 64;
    private static final int CIRCLE_DIAMETER = 40;
    private static final int MAX_ALPHA = 255;
    private DrawableCircleImageView circleImageView;
    private MaterialProgressDrawable mProgress;

    public static final int LOAD_SUCCESS = 101;
    public static final int LOAD_FAILED = 102;

    public static final int REFRESH = 1;
    public static final int LOAD = 2;

    private SwipeRefreshLoadLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private boolean mIsRefresh = false;
    private RecyclerAdapter mAdapter;

    private Context context = this;

    private PortfolioManager mPortfolioManager;
    private int page = 0;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_SUCCESS:
                    mIsRefresh = false;
                    mRefreshLayout.setRefreshing(false);
                    PortfolioVO[] portfolioVOs = (PortfolioVO[]) msg.obj;
                    if (portfolioVOs != null && portfolioVOs.length > 0) {
                        mAdapter.addLoadData(portfolioVOs);
                    }
                    page = msg.arg1 == REFRESH ? 0 : page + 1;
                    break;
                case LOAD_FAILED:
                    if (context == null) {
                        return;
                    }
                    ShowToast.makeText(context, R.drawable.emoji_cry, getString(R.string.load_failed)).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);
        mPortfolioManager = PortfolioManager.getInstance(this);
        initView();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new RecyclerAdapter(this, new RecyclerAdapter.RecyclerItemListener() {
            @Override
            public void onItemClick(View v, int position) {
                UILauncher.launchCommunityDescriptActivity(context,mAdapter.getItem(position), position);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        mRefreshLayout.setRefreshing(true);
        mIsRefresh = true;
        loadPortfolio(REFRESH);
    }

    private void initView() {
        mRefreshLayout = (SwipeRefreshLoadLayout) findViewById(R.id.community_swipe);
        mRecyclerView = (RecyclerView) findViewById(R.id.comunity_recycler);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(community_relayout);

        circleImageView = new DrawableCircleImageView(mRecyclerView.getContext(), CIRCLE_BG_LIGHT, CIRCLE_DIAMETER / 2);
        mProgress = new MaterialProgressDrawable(this, mRecyclerView);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        circleImageView.setImageDrawable(mProgress);
        circleImageView.setVisibility(View.GONE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        circleImageView.setLayoutParams(layoutParams);
        relativeLayout.addView(circleImageView);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLoadLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mIsRefresh) {
                    mIsRefresh = true;
                    loadPortfolio(REFRESH);
                }
            }
        });
        mRefreshLayout.setLoadMoreListener(new SwipeRefreshLoadLayout.LoadMoreListener() {
            @Override
            public void loadMore() {
                loadPortfolio(LOAD);
//                circleImageView.setVisibility(View.VISIBLE);
//                mProgress.setAlpha(MAX_ALPHA);
//                mProgress.start();
            }
        });
        mRefreshLayout.setColorSchemeResources(R.color.actionBarStyle);
    }

    private void loadPortfolio(final int flag) {
        mPortfolioManager.loadLatestPortfolios(page, new PortfolioLoadListener<PortfolioVO>() {
            @Override
            public void onLoaded(boolean success, boolean remaining, PortfolioVO[] dataArray) {
                Message message = Message.obtain(handler, success ? LOAD_SUCCESS : LOAD_FAILED, flag, 0, dataArray);
                handler.sendMessage(message);
            }
        });
    }

    public static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<PortfolioVO> arrayList;
        private Context context;
        private LayoutInflater mInflater;
        private RecyclerItemListener recyclerItemListener;
        private AdElementDisplay adElementDisplay;

        public RecyclerAdapter(Context context, RecyclerItemListener recyclerItemListener) {
            this.context = context;
            this.recyclerItemListener = recyclerItemListener;
            arrayList = new ArrayList<>();
            mInflater = LayoutInflater.from(context);
            adElementDisplay = AdElementDisplay.getInstance(context);
        }

        public void addLoadData(PortfolioVO[] portfolioVOs) {
            if (portfolioVOs != null && portfolioVOs.length > 0) {
                Collections.addAll(arrayList, portfolioVOs);
            }
            notifyDataSetChanged();
        }

        public PortfolioVO getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = mInflater.inflate(R.layout.item_recycler, parent, false);
            return new RecyclerViewHolder(rootView, getImageWidth(), recyclerItemListener);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            RecyclerViewHolder viewHolder = (RecyclerViewHolder) holder;
            PortfolioVO portfolioVO = getItem(position);
            viewHolder.name.setText(portfolioVO.getUserVO().getNickname());
//            viewHolder.time.setText(portfolioVO.get);
            viewHolder.content.setVisibility(View.VISIBLE);
            viewHolder.content.setText(portfolioVO.getContent());
            adElementDisplay.displayPortfolioThumbImage(portfolioVO, viewHolder.image);
        }

        public int getImageWidth() {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            return (windowManager.getDefaultDisplay().getWidth() - BmpUtils.dp2px(context, 20)) / 2;
        }

        @Override
        public int getItemCount() {
            return arrayList == null ? 0 : arrayList.size();
        }

        public static class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView name = null;
            private TextView time = null;
            private TextView content = null;
            private CircleImageView tx = null;
            private ImageView image = null;
            private RecyclerItemListener recyclerItemListener;

            public RecyclerViewHolder(View itemView, int width, RecyclerItemListener recyclerItemListener) {
                super(itemView);
                itemView.setOnClickListener(this);
                name = (TextView) itemView.findViewById(R.id.item_name);
                time = (TextView) itemView.findViewById(R.id.item_time);
                content = (TextView) itemView.findViewById(R.id.item_content);
                tx = (CircleImageView) itemView.findViewById(R.id.item_image_tx);
                image = (ImageView) itemView.findViewById(R.id.item_image);
                image.setLayoutParams(new RelativeLayout.LayoutParams(width, (int) (width / 1.5)));
                this.recyclerItemListener = recyclerItemListener;
            }

            @Override
            public void onClick(View v) {
                recyclerItemListener.onItemClick(v, getPosition());
            }

//            private String getTimeString(long time) {
//                return DateTimeUtils.getDescriptionTimeFromTimestamp(mContext, time);
//            }
        }

        public interface RecyclerItemListener {
            void onItemClick(View v, int position);
        }
    }


}
