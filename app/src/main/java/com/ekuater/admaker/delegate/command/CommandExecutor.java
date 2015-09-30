
package com.ekuater.admaker.delegate.command;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.datastruct.RequestCommand;
import com.ekuater.admaker.util.L;

/**
 * @author LinYong
 */
public class CommandExecutor implements Handler.Callback {

    private static final String TAG = CommandExecutor.class.getSimpleName();
    private static final String REAL_URL_PREFIX = "http://";

    private static final int MSG_COMMAND_EXECUTE_COMMAND = 100;
    private static final int MSG_COMMAND_EXECUTE_SUCCESS = 101;
    private static final int MSG_COMMAND_EXECUTE_FAILURE = 102;

    private volatile static CommandExecutor sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new CommandExecutor(context.getApplicationContext());
        }
    }

    public static CommandExecutor getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private static class ResultArgs {

        public final String url;
        public final ICommandHandler handler;
        public final int statusCode;
        public final String responseString;
        public final Throwable throwable;

        public ResultArgs(String url, ICommandHandler handler, int statusCode,
                          String responseString, Throwable throwable) {
            this.url = url;
            this.handler = handler;
            this.statusCode = statusCode;
            this.responseString = responseString;
            this.throwable = throwable;
        }

        public ResultArgs(String url, ICommandHandler handler, int statusCode,
                          String responseString) {
            this(url, handler, statusCode, responseString, null);
        }
    }

    private static final class CommandExecution {

        public final RequestCommand command;
        public final ICommandHandler handler;

        public CommandExecution(RequestCommand command, ICommandHandler handler) {
            this.command = command;
            this.handler = handler;
        }
    }

    private class AsyncCommandResponse extends AbstractResponse {

        private final CommandExecution execution;

        public AsyncCommandResponse(CommandExecution execution) {
            this.execution = execution;
        }

        private String getUrl() {
            return execution.command.getUrl();
        }

        private ICommandHandler getHandler() {
            return execution.handler;
        }

        private void postOnSuccess(int statusCode, String response) {
            ResultArgs args = new ResultArgs(getUrl(), getHandler(), statusCode, response);
            mProcessHandler.obtainMessage(MSG_COMMAND_EXECUTE_SUCCESS, args).sendToTarget();
        }

        private void postOnFailure(int statusCode, String response, Throwable throwable) {
            ResultArgs args = new ResultArgs(getUrl(), getHandler(), statusCode, response, throwable);
            mProcessHandler.obtainMessage(MSG_COMMAND_EXECUTE_FAILURE, args).sendToTarget();
        }

        @Override
        public void onSuccess(int statusCode, String response) {
            L.v(TAG, "onSuccess()"
                    + ", url=" + getUrl()
                    + ", statusCode=" + statusCode
                    + ", response=" + ((response != null) ? response : "null"));
            postOnSuccess(statusCode, response);
        }

        @Override
        public void onFailure(int statusCode, String response, Throwable throwable) {
            L.v(TAG, "onFailure()"
                    + ", url=" + getUrl()
                    + ", statusCode=" + statusCode
                    + ", response=" + ((response != null) ? response : "null")
                    + ", throwable=" + ((throwable != null) ? throwable : "null"));
            postOnFailure(statusCode, response, throwable);
        }
    }

    private final Context mContext;
    private final HandlerThread mProcessThread;
    private final Handler mProcessHandler;
    private final ICommandClient mCmdClient;
    private final String mBaseUrl;

    private CommandExecutor(Context context) {
        mContext = context;
        mProcessThread = new HandlerThread("CommandExecutor");
        mProcessThread.start();
        mProcessHandler = new Handler(mProcessThread.getLooper(), this);
        mCmdClient = ClientFactory.getDefaultClient();
        mBaseUrl = getBaseUrl();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mProcessThread.getLooper().quit();
        mProcessThread.quit();
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_COMMAND_EXECUTE_COMMAND:
                executeInternal((CommandExecution) msg.obj);
                break;
            case MSG_COMMAND_EXECUTE_SUCCESS:
                onCommandSuccess((ResultArgs) msg.obj);
                break;
            case MSG_COMMAND_EXECUTE_FAILURE:
                onCommandFailure((ResultArgs) msg.obj);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    public void execute(@NonNull RequestCommand command, @NonNull ICommandHandler handler) {
        L.v(TAG, "execute(), command=%1$s", command.toString());
        mProcessHandler.obtainMessage(MSG_COMMAND_EXECUTE_COMMAND,
                new CommandExecution(command, handler))
                .sendToTarget();
    }

    private void onCommandSuccess(ResultArgs args) {
        try {
            args.handler.onResponse(ConstantCode.EXECUTE_RESULT_SUCCESS,
                    args.responseString);
        } catch (Exception e) {
            L.w(TAG, "onCommandSuccess()", e);
        }
    }

    private void onCommandFailure(ResultArgs args) {
        try {
            args.handler.onResponse(ConstantCode.EXECUTE_RESULT_NETWORK_ERROR,
                    args.responseString);
        } catch (Exception e) {
            L.w(TAG, "onCommandFailure()", e);
        }
    }

    private void executeInternal(CommandExecution execution) {
        AsyncCommandResponse response = new AsyncCommandResponse(execution);
        String realUrl = parseRealUrl(execution.command.getUrl());

        switch (execution.command.getRequestMethod()) {
            case ConstantCode.REQUEST_GET: {
                mCmdClient.get(realUrl, execution.command.getHeaders(),
                        execution.command.getParam(), response);
                break;
            }
            case ConstantCode.REQUEST_POST: {
                mCmdClient.post(realUrl, execution.command.getHeaders(),
                        execution.command.getParam(), response);
                break;
            }
            case ConstantCode.REQUEST_PUT: {
                mCmdClient.put(realUrl, execution.command.getHeaders(),
                        execution.command.getParam(), response);
                break;
            }
            case ConstantCode.REQUEST_DELETE: {
                mCmdClient.delete(realUrl, execution.command.getHeaders(),
                        execution.command.getParam(), response);
                break;
            }
            default: {
                mCmdClient.post(realUrl, execution.command.getHeaders(),
                        execution.command.getParam(), response);
                break;
            }
        }
    }

    private String getBaseUrl() {
        String baseUrl = mContext.getResources().getString(
                R.string.config_http_api_base_url);

        if (TextUtils.isEmpty(baseUrl)) {
            throw new IllegalArgumentException("empty base api url");
        }

        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String parseRealUrl(String url) {
        String realUrl = url;

        if (!TextUtils.isEmpty(url) && !url.startsWith(REAL_URL_PREFIX)) {
            StringBuilder sb = new StringBuilder();
            sb.append(mBaseUrl);
            if (!url.startsWith("/")) {
                sb.append("/");
            }
            sb.append(url);
            realUrl = sb.toString();
        }

        return realUrl;
    }
}
