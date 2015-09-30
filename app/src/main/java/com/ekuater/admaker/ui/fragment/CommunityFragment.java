package com.ekuater.admaker.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.datastruct.SimpleUserVO;
import com.ekuater.admaker.datastruct.eventbus.PortfolioChangeEvent;
import com.ekuater.admaker.datastruct.eventbus.PortfolioPublishedEvent;
import com.ekuater.admaker.datastruct.eventbus.PublishNotLoginEvent;
import com.ekuater.admaker.delegate.AccountManager;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.NormalCallListener;
import com.ekuater.admaker.delegate.PortfolioLoadListener;
import com.ekuater.admaker.delegate.PortfolioManager;
import com.ekuater.admaker.delegate.ProgressListener;
import com.ekuater.admaker.ui.TakePhotoHelper;
import com.ekuater.admaker.ui.UIEventBusHub;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.holder.BaseViewHolder;
import com.ekuater.admaker.ui.holder.ItemListener;
import com.ekuater.admaker.ui.util.DateTimeUtils;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.ui.widget.CircleImageView;
import com.ekuater.admaker.ui.widget.SendingProgressView;
import com.ekuater.admaker.ui.widget.SwipeRefreshLoadLayout;
import com.ekuater.admaker.util.BmpUtils;

import java.util.ArrayList;
import java.util.Collections;

import de.greenrobot.event.EventBus;
import me.drakeet.materialdialog.MaterialDialog;
import menu.animotion.PromotedActionsLibrary;

/**
 * Created by Administrator on 2015/7/3.
 *
 * @author Xu Wenxiang
 */
public class CommunityFragment extends Fragment implements Handler.Callback {

    public static final int LOAD_SUCCESS = 101;
    public static final int LOAD_FAILED = 102;
    public static final int PUSH_SUCCESS = 103;
    public static final int PUSH_FAILED = 104;

    public static final int REFRESH = 1;
    public static final int LOAD = 2;

    private SwipeRefreshLoadLayout mRefreshLayout;
    private boolean mIsRefresh = false;
    private RecyclerAdapter mAdapter;

    private Context context;

    private PortfolioManager mPortfolioManager;
    private int page = 1;
    private EventBus mUIEventBus;
    private TakePhotoHelper mTakePhotoHelper;
    private LayoutInflater mInflater;

    private Handler handler = new Handler(this);

    private SendingProgressView mSendingProgressView;
    private View mSendingProgressBgView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        mUIEventBus = UIEventBusHub.getDefaultEventBus();
        mUIEventBus.register(this);
        mPortfolioManager = PortfolioManager.getInstance(getActivity());
        mTakePhotoHelper = new TakePhotoHelper(this, new TakePhotoHelper.TakeListener() {
            @Override
            public void onToken(@Nullable Bitmap bitmap) {
                showDialog(bitmap);
            }
        });
        mAdapter = new RecyclerAdapter(context, new ItemListener.RecyclerItemListener() {
            @Override
            public void onItemClick(View v, int position) {
                UILauncher.launchCommunityDescriptActivity(context, mAdapter.getItem(position), position);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_community, container, false);
        stepMenu(view);
        mRefreshLayout = (SwipeRefreshLoadLayout) view.findViewById(R.id.community_swipe);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.comunity_recycler);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mRefreshLayout.setColorSchemeResources(R.color.actionBarStyle);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        mRefreshLayout.setRefreshing(true);
        mIsRefresh = true;
        loadPortfolio(REFRESH);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLoadLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mIsRefresh) {
                    page = 1;
                    mIsRefresh = true;
                    loadPortfolio(REFRESH);
                }
            }
        });
        mRefreshLayout.setLoadMoreListener(new SwipeRefreshLoadLayout.LoadMoreListener() {
            @Override
            public void loadMore() {
                if (!mIsRefresh) {
                    mIsRefresh = true;
                    loadPortfolio(LOAD);
                }
            }
        });

        mSendingProgressView = (SendingProgressView) view.findViewById(R.id.sending_progress);
        mSendingProgressView.setOnLoadingFinishedListener(new SendingProgressView
                .OnLoadingFinishedListener() {
            @Override
            public void onLoadingFinished() {
                onSendingProgressFinished();
            }
        });
        mSendingProgressBgView = view.findViewById(R.id.sending_progress_bg);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUIEventBus != null) {
            mUIEventBus.unregister(this);
        }
    }

    private void stepMenu(View view) {
        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.container);
        PromotedActionsLibrary promotedActionsLibrary = new PromotedActionsLibrary();
        promotedActionsLibrary.setup(getActivity().getApplicationContext(), frameLayout);
        promotedActionsLibrary.addMainItem(getResources().getDrawable(
                        android.R.drawable.ic_menu_camera),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (AccountManager.getInstance(context).isLogin()) {
                            mTakePhotoHelper.takePhoto();
                        } else {
                            UIEventBusHub.getDefaultEventBus().post(new PublishNotLoginEvent());
                        }
                    }
                });
    }

    private void showDialog(final Bitmap bitmap) {
        @SuppressLint("InflateParams")
        View rootView = mInflater.inflate(R.layout.push_community_dialog, null, false);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.push_dialog_image);
        imageView.setImageBitmap(bitmap);
        final MaterialDialog dialog = new MaterialDialog(getActivity());
        dialog.setContentView(rootView);
        dialog.setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton(getString(R.string.push), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                publishBitmap(bitmap);
            }
        });
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mTakePhotoHelper.onActivityResult(requestCode, resultCode, data);
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

    private void publishBitmap(Bitmap bitmap) {
        startSendingProgress();
        mPortfolioManager.publishPortfolio(bitmap, "",
                new NormalCallListener() {
                    @Override
                    public void onCallResult(boolean success) {
                        handler.obtainMessage(success ? PUSH_SUCCESS : PUSH_FAILED)
                                .sendToTarget();
                    }
                },
                new ProgressListener() {
                    @Override
                    public void onProgress(double percent) {
                        updateSendingProgress((float) percent);
                    }
                });
    }

    private void startSendingProgress() {
        mSendingProgressView.setVisibility(View.VISIBLE);
        mSendingProgressBgView.setVisibility(View.VISIBLE);
        mSendingProgressView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mSendingProgressView.getViewTreeObserver().removeOnPreDrawListener(this);
                        mSendingProgressView.startProgress(10);
                        return true;
                    }
                });
    }

    private void updateSendingProgress(float progress) {
        mSendingProgressView.setCurrentProgress(progress * 90 + 10);
    }

    private void finishSendingProgress(boolean success) {
        mSendingProgressView.finishProgress(success);
    }

    private void onSendingProgressFinished() {
        mSendingProgressView.animate().scaleY(0).scaleX(0).setDuration(200).setStartDelay(100)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSendingProgressView.setScaleX(1);
                        mSendingProgressView.setScaleY(1);
                        mSendingProgressView.setVisibility(View.GONE);
                    }
                }).start();
        mSendingProgressBgView.animate().alpha(0.f).setDuration(200).setStartDelay(100)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSendingProgressBgView.setAlpha(1);
                        mSendingProgressBgView.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    private void onHandlerLoadPortfolio(Message msg) {
        mIsRefresh = false;
        if (msg.arg1 == REFRESH) {
            mRefreshLayout.setRefreshing(false);
        } else if (msg.arg1 == LOAD) {
            mRefreshLayout.setLoadMore(false);
        }
        PortfolioVO[] portfolioVOs = (PortfolioVO[]) msg.obj;
        if (portfolioVOs != null && portfolioVOs.length > 0) {
            mAdapter.addLoadData(portfolioVOs, msg.arg1);
            page++;
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(PortfolioChangeEvent event) {
        PortfolioVO portfolio = event.getPortfolio();
        int position = event.getPosition();
        PortfolioVO itemPortfolioVO = mAdapter.getItem(position);
        if (portfolio != null && itemPortfolioVO != null) {
            itemPortfolioVO.setCommentNum(portfolio.getCommentNum());
            itemPortfolioVO.setPraiseNum(portfolio.getPraiseNum());
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(PortfolioPublishedEvent event) {
        if (!mIsRefresh) {
            page = 1;
            mIsRefresh = true;
            mRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue
                    .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                            .getDisplayMetrics()));
            mRefreshLayout.setRefreshing(true);
            loadPortfolio(REFRESH);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case LOAD_SUCCESS:
                onHandlerLoadPortfolio(msg);
                break;
            case LOAD_FAILED:
                ShowToast.makeText(context, R.drawable.emoji_cry,
                        context.getString(R.string.load_failed)).show();
                break;
            case PUSH_SUCCESS:
                if (!mIsRefresh) {
                    page = 1;
                    mIsRefresh = true;
                    loadPortfolio(REFRESH);
                    UIEventBusHub.getDefaultEventBus().post(new PortfolioPublishedEvent());
                }
                finishSendingProgress(true);
                break;
            case PUSH_FAILED:
                finishSendingProgress(false);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    public static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<PortfolioVO> arrayList;
        private Context context;
        private LayoutInflater mInflater;
        private ItemListener.RecyclerItemListener itemListener;
        private AdElementDisplay adElementDisplay;

        public RecyclerAdapter(Context context, ItemListener.RecyclerItemListener itemListener) {
            this.context = context;
            this.itemListener = itemListener;
            arrayList = new ArrayList<>();
            mInflater = LayoutInflater.from(context);
            adElementDisplay = AdElementDisplay.getInstance(context);
        }

        public void addLoadData(PortfolioVO[] portfolioVOs, int flag) {
            if (portfolioVOs != null && portfolioVOs.length > 0) {
                if (flag == CommunityFragment.REFRESH) {
                    arrayList.clear();
                }
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
            return new RecyclerViewHolder(rootView, getImageWidth(), itemListener);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            RecyclerViewHolder viewHolder = (RecyclerViewHolder) holder;
            PortfolioVO portfolioVO = getItem(position);
            if (portfolioVO != null) {
                SimpleUserVO userVO = portfolioVO.getUserVO();
                if (userVO != null) {
                    viewHolder.name.setText(portfolioVO.getUserVO().getNickname());
                    adElementDisplay.displayOnlineImage(portfolioVO.getUserVO().getAvatarThumb(), viewHolder.tx, R.drawable.contact_single);
                }
                viewHolder.time.setText(getTimeString(portfolioVO.getCreateDate()));
                adElementDisplay.displayPortfolioThumbImage(portfolioVO, viewHolder.image, R.drawable.pic_loading);
            }
        }

        public int getImageWidth() {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            return (windowManager.getDefaultDisplay().getWidth() - BmpUtils.dp2px(context, 12)) / 2;
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getTimeString(context, time).trim();
        }

        @Override
        public int getItemCount() {
            return arrayList == null ? 0 : arrayList.size();
        }

        public static class RecyclerViewHolder extends BaseViewHolder {

            private TextView name = null;
            private TextView time = null;
            private CircleImageView tx = null;
            private ImageView image = null;

            public RecyclerViewHolder(View itemView, int width, ItemListener.RecyclerItemListener itemListener) {
                super(itemView, itemListener);
                name = (TextView) itemView.findViewById(R.id.item_name);
                time = (TextView) itemView.findViewById(R.id.item_time);
                tx = (CircleImageView) itemView.findViewById(R.id.item_image_tx);
                image = (ImageView) itemView.findViewById(R.id.item_image);
                image.setLayoutParams(new RelativeLayout.LayoutParams(width, (int) (width / 1.5)));
                ((RippleView) itemView.findViewById(R.id.comment_rippleview)).setOnRippleCompleteListener(
                        new RippleView.OnRippleCompleteListener() {
                            @Override
                            public void onComplete(RippleView rippleView) {
                                mItemListener.onItemClick(null, getLayoutPosition());
                            }
                        });
            }

            @Override
            protected void onRecyclerClick(View v) {
            }
        }
    }
}
