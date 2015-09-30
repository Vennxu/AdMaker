package com.ekuater.admaker.delegate;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.command.portfolio.CommentPortfolioCommand;
import com.ekuater.admaker.command.portfolio.DeletePortfolioCommand;
import com.ekuater.admaker.command.portfolio.LatestPortfolioCommand;
import com.ekuater.admaker.command.portfolio.PortfolioCommentsCommand;
import com.ekuater.admaker.command.portfolio.PraisePortfolioCommand;
import com.ekuater.admaker.command.portfolio.RequestUploadPortfolioUrlCommand;
import com.ekuater.admaker.command.portfolio.UserPortfolioCommand;
import com.ekuater.admaker.datastruct.PortfolioCommentVO;
import com.ekuater.admaker.datastruct.PortfolioVO;
import com.ekuater.admaker.settings.Settings;
import com.ekuater.admaker.util.BmpUtils;
import com.ekuater.admaker.util.L;

import java.io.File;
import java.util.Locale;

/**
 * Created by Leo on 2015/7/2.
 *
 * @author LinYong
 */
public class PortfolioManager extends BaseManager {

    private static final String TAG = PortfolioManager.class.getSimpleName();

    private volatile static PortfolioManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new PortfolioManager(context.getApplicationContext());
        }
    }

    public static PortfolioManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private Handler mProcessHandler;
    private ContentResolver mCR;

    protected PortfolioManager(Context context) {
        super(context);
        mProcessHandler = new Handler(getProcessLooper());
        mCR = context.getContentResolver();
    }

    /**
     * Load latest portfolios from server
     *
     * @param page     page index, start with 1
     * @param listener load data listener, can not be null
     */
    public void loadLatestPortfolios(final int page,
                                     @NonNull final PortfolioLoadListener<PortfolioVO> listener) {
        loadLatestPortfolios(page, 20, listener);
    }

    /**
     * Load latest portfolios from server
     *
     * @param page     page index, start with 1
     * @param pageSize page size
     * @param listener load data listener, can not be null
     */
    public void loadLatestPortfolios(final int page, final int pageSize,
                                     @NonNull final PortfolioLoadListener<PortfolioVO> listener) {
        new CommandCall<LatestPortfolioCommand, LatestPortfolioCommand.Response>(
                this, mProcessHandler) {

            @Override
            protected LatestPortfolioCommand setupCommand() {
                LatestPortfolioCommand command = new LatestPortfolioCommand(page);
                command.putParamPageSize(pageSize);
                return command;
            }

            @Override
            protected void onCallResult(boolean success,
                                        LatestPortfolioCommand.Response response) {
                PortfolioVO[] portfolioVOs = response != null ? response.getPortfolioVOs() : null;
                int length = portfolioVOs != null ? portfolioVOs.length : 0;
                boolean remaining = length >= LatestPortfolioCommand.COUNT_PER_PAGE;
                listener.onLoaded(success, remaining, portfolioVOs);
            }
        }.call();
    }

    /**
     * Load user portfolios from server
     *
     * @param queryUserId query userId
     * @param page        page index, start with 1
     * @param listener    load data listener, can not be null
     */
    public void loadUserPortfolios(final String queryUserId, final int page,
                                   @NonNull final PortfolioLoadListener<PortfolioVO> listener) {
        new CommandCall<UserPortfolioCommand, UserPortfolioCommand.Response>(
                this, mProcessHandler) {

            @Override
            protected UserPortfolioCommand setupCommand() {
                UserPortfolioCommand command = new UserPortfolioCommand();
                command.putParamUserId(getUserId());
                command.putParamQueryUserId(queryUserId);
                command.putParamPage(page);
                return command;
            }

            @Override
            protected void onCallResult(boolean success,
                                        UserPortfolioCommand.Response response) {
                PortfolioVO[] portfolioVOs = response != null ? response.getPortfolioVOs() : null;
                int length = portfolioVOs != null ? portfolioVOs.length : 0;
                boolean remaining = length >= UserPortfolioCommand.COUNT_PER_PAGE;
                listener.onLoaded(success, remaining, portfolioVOs);
            }
        }.call();
    }

    /**
     * load portfolio comments from server
     *
     * @param portfolioId portfolio id
     * @param page        page index, start with 1
     * @param listener    load data listener, can not be null,
     *                    PortfolioVO in dataArray[0], may be null
     */
    public void loadPortfolioComments(final String portfolioId, final int page,
                                      @NonNull final PortfolioLoadListener<PortfolioVO> listener) {
        new CommandCall<PortfolioCommentsCommand, PortfolioCommentsCommand.Response>(
                this, mProcessHandler) {

            @Override
            protected PortfolioCommentsCommand setupCommand() {
                PortfolioCommentsCommand command = new PortfolioCommentsCommand(getUserToken());
                command.putParamPortfolioId(portfolioId);
                command.putParamPage(page);
                return command;
            }

            @Override
            protected void onCallResult(boolean success,
                                        PortfolioCommentsCommand.Response response) {
                PortfolioVO portfolioVO = response != null ? response.getPortfolioVO() : null;
                PortfolioCommentVO[] commentVOs = portfolioVO != null
                        ? portfolioVO.getCommentVOs() : null;
                int length = commentVOs != null ? commentVOs.length : 0;
                boolean remaining = length >= PortfolioCommentsCommand.COUNT_PER_PAGE;
                listener.onLoaded(success, remaining, new PortfolioVO[]{portfolioVO});
            }
        }.call();
    }

    /**
     * comment a portfolio
     *
     * @param portfolioId portfolio id
     * @param comment     comment content
     * @param listener    load data listener, can not be null,
     *                    PortfolioCommentVO in dataArray[0], may be null
     */
    public void commentPortfolio(final String portfolioId, final String comment,
                                 @NonNull
                                 final PortfolioLoadListener<PortfolioCommentVO> listener) {
        commentPortfolio(portfolioId, comment, null, null, null, null, listener);
    }

    /**
     * comment a portfolio
     *
     * @param portfolioId     portfolio id
     * @param comment         comment content
     * @param parentCommentId comment id which reply
     * @param replyComment    comment content which reply
     * @param replyNickname   comment user nickname which reply
     * @param replyUserId     comment user id which reply
     * @param listener        load data listener, can not be null,
     *                        PortfolioCommentVO in dataArray[0], may be null
     */
    public void commentPortfolio(final String portfolioId, final String comment,
                                 final String parentCommentId, final String replyComment,
                                 final String replyNickname, final String replyUserId,
                                 @NonNull
                                 final PortfolioLoadListener<PortfolioCommentVO> listener) {
        new CommandCall<CommentPortfolioCommand, CommentPortfolioCommand.Response>(
                this, mProcessHandler) {

            @Override
            protected CommentPortfolioCommand setupCommand() {
                CommentPortfolioCommand command = new CommentPortfolioCommand(getUserToken());
                command.putParamPortfolioId(portfolioId);
                command.putParamComment(comment);
                command.putParamParentCommentId(parentCommentId);
                command.putParamReplyComment(replyComment);
                command.putParamReplyNickname(replyNickname);
                command.putParamReplyUserId(replyUserId);
                return command;
            }

            @Override
            protected void onCallResult(boolean success,
                                        CommentPortfolioCommand.Response response) {
                PortfolioCommentVO commentVO = response != null ? response.getCommentVO() : null;
                listener.onLoaded(success, false, new PortfolioCommentVO[]{commentVO});
            }
        }.call();
    }

    /**
     * praise portfolio
     *
     * @param portfolioId portfolio id
     * @param listener    praise result listener
     */
    public void praisePortfolio(final String portfolioId, final NormalCallListener listener) {
        new CommandCall<PraisePortfolioCommand, PraisePortfolioCommand.Response>(
                this, mProcessHandler) {

            @Override
            protected boolean onPreSetupCommand() {
                boolean praised = isPortfolioPraised(portfolioId);
                if (praised) {
                    notifyCallResult(listener, true);
                }
                return praised;
            }

            @Override
            protected PraisePortfolioCommand setupCommand() {
                PraisePortfolioCommand command = new PraisePortfolioCommand();
                command.putParamPortfolioId(portfolioId);
                return command;
            }

            @Override
            protected void onCallResult(boolean success,
                                        PraisePortfolioCommand.Response response) {
                if (success) {
                    setPortfolioPraised(portfolioId);
                }
                notifyCallResult(listener, success);
            }
        }.call();
    }

    /**
     * check portfolio praised or not
     *
     * @param portfolioId portfolio id
     * @return praised or not
     */
    public boolean isPortfolioPraised(final String portfolioId) {
        return Settings.Volatile.getBoolean(mCR, getPortfolioPraisedKey(portfolioId), false);
    }

    private void setPortfolioPraised(final String portfolioId) {
        Settings.Volatile.putBoolean(mCR, getPortfolioPraisedKey(portfolioId), true);
    }

    private String getPortfolioPraisedKey(final String portfolioId) {
        return String.format(Locale.ENGLISH, "portfolio_praised:%1$s", portfolioId);
    }

    /**
     * praise delete
     *
     * @param portfolioId portfolio id
     * @param listener    delete result listener
     */
    public void deletePortfolio(final String portfolioId, final NormalCallListener listener) {
        new CommandCall<DeletePortfolioCommand, DeletePortfolioCommand.Response>(
                this, mProcessHandler) {

            @Override
            protected DeletePortfolioCommand setupCommand() {
                DeletePortfolioCommand command = new DeletePortfolioCommand(getUserToken());
                command.putParamPortfolioId(portfolioId);
                return command;
            }

            @Override
            protected void onCallResult(boolean success,
                                        DeletePortfolioCommand.Response response) {
                notifyCallResult(listener, success);
            }
        }.call();
    }

    public void publishPortfolio(@NonNull final Bitmap portfolioBmp,
                                 @Nullable final String content,
                                 final NormalCallListener listener) {
        publishPortfolio(portfolioBmp, content, listener, null);
    }

    public void publishPortfolio(@NonNull final Bitmap portfolioBmp,
                                 @Nullable final String content,
                                 final NormalCallListener listener,
                                 final ProgressListener progressListener) {
        mProcessHandler.post(new Runnable() {
            @Override
            public void run() {
                publishPortfolioInternal(portfolioBmp, content, listener, progressListener);
            }
        });
    }

    public void publishPortfolio(@NonNull final File portfolioFile,
                                 @NonNull final String fileExtName,
                                 @Nullable final String content,
                                 final NormalCallListener listener) {
        publishPortfolio(portfolioFile, fileExtName, content, listener, null);
    }

    public void publishPortfolio(@NonNull final File portfolioFile,
                                 @NonNull final String fileExtName,
                                 @Nullable final String content,
                                 final NormalCallListener listener,
                                 final ProgressListener progressListener) {
        mProcessHandler.post(new Runnable() {
            @Override
            public void run() {
                publishPortfolioInternal(portfolioFile, fileExtName, content,
                        listener, progressListener);
            }
        });
    }

    private void publishPortfolioInternal(@NonNull final Bitmap portfolioBmp,
                                          @Nullable final String content,
                                          final NormalCallListener listener,
                                          final ProgressListener progressListener) {
        if (!portfolioBmp.isRecycled()) {
            try {
                final File tmpFile = File.createTempFile("portfolio", null);
                final NormalCallListener callListener = new NormalCallListener() {
                    @Override
                    public void onCallResult(boolean success) {
                        //noinspection ResultOfMethodCallIgnored
                        tmpFile.delete();
                        notifyCallResult(listener, success);
                    }
                };
                BmpUtils.saveBitmapToFile(portfolioBmp, tmpFile);
                publishPortfolioInternal(tmpFile, "jpg", content,
                        callListener, progressListener);
            } catch (Exception e) {
                L.w(TAG, "publishPortfolioInternal()", e);
                notifyCallResult(listener, false);
            }
        } else {
            notifyCallResult(listener, false);
        }
    }

    private void publishPortfolioInternal(@NonNull final File portfolioFile,
                                          @NonNull final String fileExtName,
                                          @Nullable final String content,
                                          final NormalCallListener listener,
                                          final ProgressListener progressListener) {
        if (portfolioFile.isFile()) {
            final UploadListener uploadListener = new UploadListener() {
                @Override
                public void onProgress(double percent) {
                    notifyProgress(progressListener, percent);
                }

                @Override
                public void onComplete(boolean success, String response) {
                    BaseCommand.Response resp = fromJson(response, BaseCommand.Response.class);
                    boolean publishSuccess = success && resp != null && resp.requestSuccess();
                    notifyCallResult(listener, publishSuccess);
                }
            };
            final RequestUploadUrlListener requestListener = new RequestUploadUrlListener() {
                @Override
                public void onRequestResult(boolean success, String token, String key) {
                    if (success) {
                        uploadFileToQiNiu(portfolioFile, key, token, uploadListener);
                    } else {
                        notifyCallResult(listener, false);
                    }
                }
            };
            requestUploadPortfolioUrl(content, filterExtName(fileExtName), requestListener);
        } else {
            notifyCallResult(listener, false);
        }
    }

    private String filterExtName(final String extName) {
        if (extName != null && extName.startsWith(".")) {
            String temp = extName;

            do {
                temp = temp.substring(1);
            } while (temp.startsWith("."));
            return temp;
        } else {
            return extName;
        }
    }

    private void requestUploadPortfolioUrl(final String content, final String extName,
                                           @NonNull
                                           final RequestUploadUrlListener listener) {
        new CommandCall<RequestUploadPortfolioUrlCommand,
                RequestUploadPortfolioUrlCommand.Response>(
                this, mProcessHandler) {

            @Override
            protected RequestUploadPortfolioUrlCommand setupCommand() {
                RequestUploadPortfolioUrlCommand command
                        = new RequestUploadPortfolioUrlCommand(getUserToken());
                command.putParamContent(content);
                command.putParamExtName(extName);
                return command;
            }

            @Override
            protected void onCallResult(boolean success,
                                        RequestUploadPortfolioUrlCommand.Response response) {
                String qiNiuToken = response != null ? response.getQiNiuToken() : null;
                String qiNiuKey = response != null ? response.getQiNiuKey() : null;
                listener.onRequestResult(success && !TextUtils.isEmpty(qiNiuToken)
                                && !TextUtils.isEmpty(qiNiuKey),
                        qiNiuToken,
                        qiNiuKey);
            }
        }.call();
    }

    private void notifyCallResult(NormalCallListener listener, boolean success) {
        if (listener != null) {
            listener.onCallResult(success);
        }
    }

    private void notifyProgress(ProgressListener listener, double percent) {
        if (listener != null) {
            listener.onProgress(percent);
        }
    }
}
