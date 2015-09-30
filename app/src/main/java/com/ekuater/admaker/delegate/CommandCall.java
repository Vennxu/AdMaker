package com.ekuater.admaker.delegate;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.delegate.command.ICommandHandler;
import com.ekuater.admaker.delegate.event.UnauthorizedEvent;
import com.ekuater.admaker.util.JsonUtils;
import com.ekuater.admaker.util.L;

import java.lang.reflect.ParameterizedType;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
abstract class CommandCall<Command extends BaseCommand,
        Response extends BaseCommand.Response>
        implements ICommandHandler {

    private static final String TAG = CommandCall.class.getSimpleName();

    private final ICommandClient client;
    private final Handler handler;

    public CommandCall(@NonNull ICommandClient client, @Nullable Handler handler) {
        this.client = client;
        this.handler = handler;
    }

    protected abstract Command setupCommand();

    /**
     * pre command call process
     *
     * @return true to block the command call
     */
    protected boolean onPreSetupCommand() {
        return false;
    }

    @SuppressWarnings("UnusedParameters")
    protected void onPostSetupCommand(Command command) {
    }

    public final void call() {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    doCall();
                }
            });
        } else {
            doCall();
        }
    }

    private void doCall() {
        if (!onPreSetupCommand()) {
            Command command = setupCommand();
            onPostSetupCommand(command);
            client.executeCommand(command, this);
        }
    }

    @Override
    public final void onResponse(int result, final String response) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    doResponse(response);
                }
            });
        } else {
            doResponse(response);
        }
    }

    protected void doResponse(String response) {
        boolean success = false;
        Response resp = null;

        try {
            resp = newResponse(response);
            if (resp != null) {
                success = resp.requestSuccess();
                if (resp.isUnauthorized()) {
                    EventBusHub.getDefaultEventBus().post(new UnauthorizedEvent());
                }
            }
        } catch (Exception e) {
            L.w(TAG, "doResponse(), error", e);
        } finally {
            onCallResult(success, resp);
        }
    }

    protected Response newResponse(@Nullable String response) throws Exception {
        return TextUtils.isEmpty(response) ? null
                : JsonUtils.fromJson(response, getResponseClass());
    }

    @SuppressWarnings("unchecked")
    private Class<Response> getResponseClass() {
        return (Class<Response>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];
    }

    protected abstract void onCallResult(boolean success, @Nullable Response response);
}
