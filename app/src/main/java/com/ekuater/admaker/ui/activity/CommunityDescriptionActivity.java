package com.ekuater.admaker.ui.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.PortfolioCommentVO;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.datastruct.SimpleUserVO;
import com.ekuater.admaker.datastruct.UserVO;
import com.ekuater.admaker.datastruct.UserVOUtils;
import com.ekuater.admaker.datastruct.eventbus.PortfolioChangeEvent;
import com.ekuater.admaker.delegate.AccountManager;
import com.ekuater.admaker.delegate.AdElementDisplay;
import com.ekuater.admaker.delegate.NormalCallListener;
import com.ekuater.admaker.delegate.PortfolioLoadListener;
import com.ekuater.admaker.delegate.PortfolioManager;
import com.ekuater.admaker.ui.ContentSharer;
import com.ekuater.admaker.ui.ShareContent;
import com.ekuater.admaker.ui.UILauncher;
import com.ekuater.admaker.ui.activity.base.BackIconActivity;
import com.ekuater.admaker.ui.activity.base.BaseActivity;
import com.ekuater.admaker.ui.fragment.SelectDialogFragment;
import com.ekuater.admaker.ui.fragment.SimpleProgressHelper;
import com.ekuater.admaker.ui.holder.ItemListener;
import com.ekuater.admaker.ui.util.ShowToast;
import com.ekuater.admaker.ui.widget.DetectTouchGestureLayout;
import com.ekuater.admaker.ui.widget.KeyboardStateView;
import com.ekuater.admaker.ui.widget.SendCommentButton;
import com.ekuater.admaker.ui.widget.SwipeRefreshLoadLayout;
import com.ekuater.admaker.util.BmpUtils;
import com.ekuater.admaker.util.TextUtil;

import de.greenrobot.event.EventBus;


/**
 * Created by Administrator on 2015/7/3.
 */
public class CommunityDescriptionActivity extends BackIconActivity implements View.OnClickListener,
        KeyboardStateView.OnKeyboardStateChangedListener {

    public static final int REFRESH = 0;
    public static final int LOAD = 1;

    public static final String PORTFOLIOVO = "portfolioVo";
    public static final String PORTFOLIOVO_INDEX = "portfolioVo_index";
    public static final int COMMUNITY_PRAISE_SUCCESS = 101;
    public static final int COMMUNITY_PRAISE_FAILED = 102;
    public static final int COMMUNITY_COMMENT_SUCCESS = 103;
    public static final int COMMUNITY_COMMENT_FAILED = 104;
    public static final int COMMUNITY_LOAD_COMMENT_SUCCESS = 105;
    public static final int COMMUNITY_LOAD_COMMENT_FAILED = 106;

    private RecyclerView mCommentListView;
    private SwipeRefreshLoadLayout mSwipeRefresh;
    private SendCommentButton mSendBtn;
    private EditText mInputEdit;
    private TextView mInputEditHint;
    private RelativeLayout mKeyBoardDismiss;
    private KeyboardStateView mKeyBoardStateView;

    private CommunityDescriptionAdapter mAdapter;
    private PortfolioVO mPortfolioVO;
    private AdElementDisplay mAdElementDisplay;
    private DisplayMetrics mDisplayMetrics;
    private PortfolioManager mPortfolioManager;
    private InputMethodManager mInputMethodManager;
    private AccountManager mAccountManager;
    private SimpleProgressHelper mSimpleProgressHelper;
    private PortfolioCommentVO mReplyCommentVo;
    private Context mContext;
    private ContentSharer mContentSharer;
    private int mPage = 1;
    private int mReplyFlagsCount = 0;
    private boolean isRefresh = false;
    private int mPortfolioIndex = 0;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case COMMUNITY_PRAISE_SUCCESS:
                    onHandlerPraiseResult();
                    break;
                case COMMUNITY_PRAISE_FAILED:
                    onHandlerLoadFailed(getString(R.string.load_failed), R.drawable.emoji_cry);
                    break;
                case COMMUNITY_COMMENT_SUCCESS:
                    mSimpleProgressHelper.dismiss();
                    onHandlerCommentResult(msg);
                    break;
                case COMMUNITY_COMMENT_FAILED:
                    mSimpleProgressHelper.dismiss();
                    onHandlerLoadFailed(getString(R.string.comment_failed), R.drawable.emoji_cry);
                    break;
                case COMMUNITY_LOAD_COMMENT_SUCCESS:
                    onHandlerLoadCommentResult(msg);
                    break;
                case COMMUNITY_LOAD_COMMENT_FAILED:
                    onHandlerLoadFailed(getString(R.string.load_failed), R.drawable.emoji_cry);
                    break;
                default:
                    break;
            }
        }
    };

    private ItemListener.RecyclerItemListener mItemListener = new ItemListener.RecyclerItemListener() {
        @Override
        public void onItemClick(View v, int position) {
            showInputSoft();
            onClickReplyCommentFlag(position);
        }

        @Override
        public void onCommentClick() {
            showInputSoft();
        }

        @Override
        public void onPraiseClick(int position, PortfolioVO portfolioVO) {
            praise();
        }

        @Override
        public void onShareClick() {
            showFragmentDialog();
        }

        @Override
        public void onImageClick() {

        }

        @Override
        public void onAvatarImageClick() {
            UILauncher.launchHomePageUI(mContext, mPortfolioVO.getUserVO());
        }

        @Override
        public void onHeaderAvatarImageClick(int positon) {
            PortfolioCommentVO commentVO = mAdapter.getItem(positon);
            SimpleUserVO userVO = commentVO.getUserVO();
            if (userVO != null) {
                UILauncher.launchHomePageUI(mContext, userVO);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_descript);
        mContext = this;
        mDisplayMetrics = new DisplayMetrics();
        mPortfolioManager = PortfolioManager.getInstance(mContext);
        mSimpleProgressHelper = new SimpleProgressHelper(this);
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        mAdElementDisplay = AdElementDisplay.getInstance(mContext);
        mAccountManager = AccountManager.getInstance(mContext);
        BaseActivity baseActivity = this;
        baseActivity.setHasContentSharer();
        mContentSharer = baseActivity.getContentSharer();
        mPortfolioVO = getIntent().getParcelableExtra(PORTFOLIOVO);
        mPortfolioIndex = getIntent().getIntExtra(PORTFOLIOVO_INDEX, 0);
        initView();
        initDate();
        loadComment(REFRESH);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new PortfolioChangeEvent(mPortfolioIndex, mPortfolioVO, ""));
    }

    private void initView() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(getString(R.string.decript));
        mCommentListView = (RecyclerView) findViewById(R.id.descript_recycler);
        mSwipeRefresh = (SwipeRefreshLoadLayout) findViewById(R.id.descript_swipe);
        mInputEdit = (EditText) findViewById(R.id.input_edit);
        mInputEditHint = (TextView) findViewById(R.id.input_edit_hint);
        mSendBtn = (SendCommentButton) findViewById(R.id.input_send_btn);
        mKeyBoardDismiss = (RelativeLayout) findViewById(R.id.descript_click_cancle_input);
        mKeyBoardStateView = (KeyboardStateView) findViewById(R.id.keyboardstateview);
        ((DetectTouchGestureLayout) findViewById(R.id.gesture)).setSwipeGestureListener(new DetectTouchGestureLayout.onSwipeGestureListener() {
            @Override
            public void onLeftSwipe() {
            }

            @Override
            public void onRightSwipe() {
                finish();
            }
        });

        mInputEdit.addTextChangedListener(mTextWatcher);
        findViewById(R.id.icon).setOnClickListener(this);
        mSendBtn.setOnSendClickListener(onSendClickListener);
        mInputEditHint.setOnClickListener(this);
        mKeyBoardDismiss.setOnClickListener(this);
        mKeyBoardStateView.setOnKeyboardStateChangedListener(this);
        mInputEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    String content = mInputEdit.getText().toString();
                    if (mReplyFlagsCount != 0 && content.length() < mReplyFlagsCount) {
                        clearReplyFlag();
                    }
                }
                return false;
            }
        });
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLoadLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRefresh) {
                    isRefresh = true;
                    loadComment(REFRESH);
                }
            }
        });
        mSwipeRefresh.setLoadMoreListener(new SwipeRefreshLoadLayout.LoadMoreListener() {
            @Override
            public void loadMore() {
                if (!isRefresh) {
                    if (mAdapter.getItemCount() >= 20) {
                        isRefresh = true;
                        loadComment(LOAD);
                    }
                }
            }
        });
        mSwipeRefresh.setColorSchemeResources(R.color.actionBarStyle);
        mSwipeRefresh.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        mSwipeRefresh.setRefreshing(true);
    }

    private void clearReplyFlag() {
        mReplyFlagsCount = 0;
        mReplyCommentVo = null;
        mInputEdit.setText("");
        mInputEditHint.setText(getString(R.string.input_comment_hint));
    }

    private void initDate() {
        mAdapter = new CommunityDescriptionAdapter(this, mDisplayMetrics, mItemListener);
        mAdapter.setHeader(mPortfolioVO);
        mCommentListView.setLayoutManager(new LinearLayoutManager(this));
        mCommentListView.setAdapter(mAdapter);
    }

    private SendCommentButton.OnSendClickListener onSendClickListener = new SendCommentButton.OnSendClickListener() {
        @Override
        public void onSendClickListener(View v) {
            if (mAccountManager.isLogin()) {
                String text = mInputEdit.getText().toString();
                String content = text.substring(mReplyFlagsCount, text.length());
                if (!TextUtil.isEmpty(content)) {
                    if (mReplyCommentVo != null) {
                        comment(content, mReplyCommentVo);
                    } else {
                        comment(content);
                    }
                    mSendBtn.setCurrentState(SendCommentButton.STATE_DONE);
                }
            } else {
                onHandlerLoadFailed(getString(R.string.login_prompt), R.drawable.emoji_cry);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.icon:
                finish();
                break;
            case R.id.input_edit_hint:
                showInputSoft();
                break;
            case R.id.input_send_btn:
                if (mAccountManager.isLogin()) {
                    String text = mInputEdit.getText().toString();
                    String content = text.substring(mReplyFlagsCount, text.length());
                    if (TextUtil.isEmpty(content)) {
                        return;
                    }
                    if (mReplyCommentVo != null) {
                        comment(content, mReplyCommentVo);
                    } else {
                        comment(content);
                    }
                    mSendBtn.setCurrentState(SendCommentButton.STATE_DONE);
                } else {
                    onHandlerLoadFailed(getString(R.string.login_prompt), R.drawable.emoji_cry);
                }
                break;
            case R.id.descript_click_cancle_input:
                mInputMethodManager.hideSoftInputFromWindow(mInputEdit.getWindowToken(), 0);
                break;
            default:
                break;
        }
    }

    private void showInputSoft() {
        changeState(true);
        mInputEdit.requestFocus();
        mInputMethodManager.showSoftInput(mInputEdit, 0);
    }

    private void changeState(boolean state) {
        mInputEdit.setVisibility(state ? View.VISIBLE : View.GONE);
        mInputEditHint.setVisibility(state ? View.GONE : View.VISIBLE);
        mKeyBoardDismiss.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    private void onClickReplyCommentFlag(int position) {
        PortfolioCommentVO portfolioCommentVO = mAdapter.getItem(position);
        SimpleUserVO userVO = portfolioCommentVO != null
                ? portfolioCommentVO.getUserVO() : null;

        if (userVO != null) {
            mReplyCommentVo = portfolioCommentVO;
            String userName = userVO.getNickname();
            String relyFlags = "@" + userName + " ";
            mReplyFlagsCount = relyFlags.length();
            SpannableString ss = new SpannableString(relyFlags);
            ss.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.actionBarStyle)), 0,
                    mReplyFlagsCount - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mInputEdit.setText(ss);
            mInputEdit.setSelection(ss.length());
        }
    }

    private void loadComment(final int flag) {
        mPortfolioManager.loadPortfolioComments(mPortfolioVO.getPortfolioId(), mPage, new PortfolioLoadListener<PortfolioVO>() {
            @Override
            public void onLoaded(boolean success, boolean remaining, PortfolioVO[] dataArray) {
                Message message = Message.obtain(handler, success ? COMMUNITY_LOAD_COMMENT_SUCCESS : COMMUNITY_LOAD_COMMENT_FAILED, flag, 0, dataArray);
                handler.sendMessage(message);
            }
        });
    }

    private void comment(String content, PortfolioCommentVO commentVO) {
        mSimpleProgressHelper.show();
        mPortfolioManager.commentPortfolio(mPortfolioVO.getPortfolioId(), content, commentVO.getCommentId(), commentVO.getComment(),
                commentVO.getUserVO().getNickname(), commentVO.getUserVO().getUserId(), new PortfolioLoadListener<PortfolioCommentVO>() {
                    @Override
                    public void onLoaded(boolean success, boolean remaining, PortfolioCommentVO[] dataArray) {
                        Message message = Message.obtain(handler, success ? COMMUNITY_COMMENT_SUCCESS : COMMUNITY_COMMENT_FAILED, dataArray);
                        handler.sendMessage(message);
                    }
                });
    }

    private void comment(String content) {
        mSimpleProgressHelper.show();
        mPortfolioManager.commentPortfolio(mPortfolioVO.getPortfolioId(), content, "", "",
                "", "", new PortfolioLoadListener<PortfolioCommentVO>() {
                    @Override
                    public void onLoaded(boolean success, boolean remaining, PortfolioCommentVO[] dataArray) {
                        Message message = Message.obtain(handler, success ? COMMUNITY_COMMENT_SUCCESS : COMMUNITY_COMMENT_FAILED, dataArray);
                        handler.sendMessage(message);
                    }
                });
    }

    private void praise() {
        mPortfolioManager.praisePortfolio(mPortfolioVO.getPortfolioId(), new NormalCallListener() {
            @Override
            public void onCallResult(boolean success) {
                Message message = Message.obtain(handler, success ? COMMUNITY_PRAISE_SUCCESS : COMMUNITY_PRAISE_FAILED);
                handler.sendMessage(message);
            }
        });
    }

    @Override
    public void onKeyboardStateChanged(int state) {
        switch (state) {
            case KeyboardStateView.KEYBOARD_STATE_SHOW:
                break;
            case KeyboardStateView.KEYBOARD_STATE_HIDE:
                changeState(false);
                String content = mInputEdit.getText().toString();
                mInputEditHint.setText(TextUtil.isEmpty(content) ? getString(R.string.input_comment_hint) : content);
                break;
            default:
                break;
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mSendBtn.setEnabled(s.length() > 0);
        }
    };

    private void onHandlerCommentResult(Message msg) {
        PortfolioCommentVO[] portfolioCommentVOs = (PortfolioCommentVO[]) msg.obj;
        if (portfolioCommentVOs != null && portfolioCommentVOs.length > 0) {
            PortfolioCommentVO commentVO = portfolioCommentVOs[0];
            UserVO userVO = mAccountManager.getUserVO();

            if (commentVO != null && userVO != null) {
                commentVO.setUserVO(UserVOUtils.toSimpleUserVO(userVO));
                PortfolioVO portfolioVO = mAdapter.getHeader();
                if (portfolioVO != null) {
                    portfolioVO.setCommentNum(portfolioVO.getCommentNum() + 1);
                }
                mAdapter.addCommentVOs(commentVO);
                onHandlerLoadFailed(getString(R.string.comment_success), R.drawable.emoji_smile);
            }
        }
        clearReplyFlag();
        mInputMethodManager.hideSoftInputFromWindow(mInputEdit.getWindowToken(), 0);
    }

    private void onHandlerPraiseResult() {
        PortfolioVO portfolioVO = mAdapter.getHeader();
        if (portfolioVO != null) {
            portfolioVO.setPraiseNum(portfolioVO.getPraiseNum() + 1);
            mAdapter.notifyItemChanged(0);
        }
    }

    private void onHandlerLoadCommentResult(Message msg) {
        isRefresh = false;
        if (msg.arg1 == REFRESH) {
            mSwipeRefresh.setRefreshing(false);
        } else {
            mSwipeRefresh.setLoadMore(false);
        }
        PortfolioVO[] portfolioVOs = (PortfolioVO[]) msg.obj;
        if (portfolioVOs == null || portfolioVOs.length < 0) {
            return;
        }
        if (portfolioVOs[0] == null){
            onHandlerLoadFailed(getString(R.string.deleted), R.drawable.emoji_cry);
            return;
        }
        if (!TextUtil.isEmpty(portfolioVOs[0].getPortfolioId())) {
            mPortfolioVO = portfolioVOs[0];
            mAdapter.setHeader(mPortfolioVO);
            mAdapter.notifyItemChanged(0);
        }
        if (portfolioVOs[0].getCommentVOs() != null && portfolioVOs[0].getCommentVOs().length > 0) {
            mPage = msg.arg1 == REFRESH ? 2 : mPage + 1;
            mAdapter.addCommentVOs(portfolioVOs[0].getCommentVOs(), msg.arg1);
        }
    }

    private void onHandlerLoadImageResult(Message message){
        Bitmap[] bitmaps = (Bitmap[]) message.obj;
        if (bitmaps != null) {
            Bitmap bitmap = bitmaps[0];
            if (bitmap != null) {
                int displayWidth = mDisplayMetrics.widthPixels - BmpUtils.dp2px(this, 10);
                int bmpWidth = bitmap.getWidth();
                float sacle = (float)displayWidth/bmpWidth;
                int bmpHeight = (int)(bitmap.getHeight() * sacle);
                mAdapter.mImageView.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth, bmpHeight));
                mAdapter.mImageView.setImageBitmap(bitmap);
            }
        }
    }

    private void onHandlerLoadFailed(String text, int drawabel) {
        if (mContext == null) {
            return;
        }
        ShowToast.makeText(mContext, drawabel, text).show();
    }

    private void showFragmentDialog() {
        SelectDialogFragment shareDialogFragment = new SelectDialogFragment(R.drawable.ic_share_weixin,
                R.drawable.ic_share_wxcircle,
                getString(R.string.share_to_weixin_friend),
                getString(R.string.share_to_weixin_circle)) {
            @Override
            protected void onFistClick() {
                shareFaceBanknote(ShareContent.Platform.WEIXIN);
            }

            @Override
            protected void onTwoClick() {
                shareFaceBanknote(ShareContent.Platform.WEIXIN_CIRCLE);
            }
        };
        shareDialogFragment.show(getSupportFragmentManager(), "ShareDialogFragment");
    }

    private void shareFaceBanknote(ShareContent.Platform platform) {
        BitmapDrawable drawable = (BitmapDrawable) mAdapter.mImageView.getDrawable();
        if (drawable != null) {
            ShareContent shareContent = new ShareContent();
            shareContent.setSharePlatform(platform);
            shareContent.setShareBitmap(drawable.getBitmap());
            mContentSharer.directShareContent(shareContent);
        } else {
            onHandlerLoadFailed(getString(R.string.share_failed), R.drawable.emoji_cry);
        }
    }
}
