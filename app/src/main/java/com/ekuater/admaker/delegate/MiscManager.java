package com.ekuater.admaker.delegate;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.ekuater.admaker.command.misc.FeedbackCommand;

/**
 * Miscellaneous function of CoreService
 *
 * @author LinYong
 */
public class MiscManager extends BaseManager {

    private volatile static MiscManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new MiscManager(context.getApplicationContext());
        }
    }

    public static MiscManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private Handler mProcessHandler;

    private MiscManager(Context context) {
        super(context);
        mProcessHandler = new Handler(getProcessLooper());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void feedback(final String suggestion, final String contact,
                         final NormalCallListener listener) {
        feedback(null, null, null, suggestion, contact, listener);
    }

    public void feedback(final String userId, final String adMakerCode, final String nickname,
                         final String suggestion, final String contact,
                         final NormalCallListener listener) {
        new CommandCall<FeedbackCommand, FeedbackCommand.Response>(this, mProcessHandler) {

            @Override
            protected FeedbackCommand setupCommand() {
                FeedbackCommand command = new FeedbackCommand();
                command.putParamUserId(userId);
                command.putParamAdMakerCode(adMakerCode);
                command.putParamNickname(nickname);
                command.putParamSuggestion(suggestion);
                command.putParamContact(contact);
                return command;
            }

            @Override
            protected void onCallResult(boolean success,
                                        @Nullable
                                        FeedbackCommand.Response response) {
                notifyCallResult(listener, success);
            }
        }.call();
    }

    private void notifyCallResult(NormalCallListener listener, boolean success) {
        if (listener != null) {
            listener.onCallResult(success);
        }
    }
}
