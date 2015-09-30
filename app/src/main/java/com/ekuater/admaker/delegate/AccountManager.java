package com.ekuater.admaker.delegate;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.ekuater.admaker.command.account.BindMobileCommand;
import com.ekuater.admaker.command.account.LoginCommand;
import com.ekuater.admaker.command.account.LogoutCommand;
import com.ekuater.admaker.command.account.ModifyPasswordCommand;
import com.ekuater.admaker.command.account.RegisterCommand;
import com.ekuater.admaker.command.account.RequestAvatarUrlCommand;
import com.ekuater.admaker.command.account.ResetPasswordCommand;
import com.ekuater.admaker.command.account.ThirdLoginCommand;
import com.ekuater.admaker.command.account.UpdateInfoCommand;
import com.ekuater.admaker.delegate.event.UnauthorizedEvent;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
public class AccountManager extends BaseManager {

    private volatile static AccountManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new AccountManager(context.getApplicationContext());
        }
    }

    public static AccountManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private Handler mProcessHandler;

    protected AccountManager(Context context) {
        super(context);
        mProcessHandler = new Handler(getProcessLooper());
        EventBusHub.getDefaultEventBus().register(this);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        EventBusHub.getDefaultEventBus().unregister(this);
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(UnauthorizedEvent event) {
        if (isLogin()) {
            setUserToken(null);
            setUserPassword(null);
            updateUserVO(null);
        }
    }

    public void register(final String mobile, final String nickname, final int gender,
                         final String password, final String captcha,
                         final NormalCallListener listener) {
        new CommandCall<RegisterCommand, RegisterCommand.Response>(this, mProcessHandler) {

            @Override
            protected RegisterCommand setupCommand() {
                RegisterCommand command = new RegisterCommand();
                command.putParamMobile(mobile);
                command.putParamNickname(nickname);
                command.putParamGender(gender);
                command.putParamPassword(password);
                command.putParamCaptcha(captcha);
                return command;
            }

            @Override
            protected void onCallResult(boolean success, RegisterCommand.Response response) {
                notifyCallResult(listener, success);
            }
        }.call();
    }

    public void login(final String loginText, final String password,
                      final NormalCallListener listener) {
        new CommandCall<LoginCommand, LoginCommand.Response>(this, mProcessHandler) {

            @Override
            protected LoginCommand setupCommand() {
                LoginCommand command = new LoginCommand();
                command.putParamLoginText(loginText);
                command.putParamPassword(password);
                return command;
            }

            @Override
            protected void onCallResult(boolean success, LoginCommand.Response response) {
                if (success) {
                    setUserToken(response.getToken());
                    updateUserVO(response.getUserVO());
                }
                notifyCallResult(listener, success);
            }
        }.call();
    }

    public void logout(final NormalCallListener listener) {
        new CommandCall<LogoutCommand, LogoutCommand.Response>(this, mProcessHandler) {

            @Override
            protected LogoutCommand setupCommand() {
                return new LogoutCommand(getUserToken());
            }

            @Override
            protected void onCallResult(boolean success, LogoutCommand.Response response) {
                notifyCallResult(listener, success);
            }
        }.call();
    }

    public void updateUserInfo() {
        // TODO
        new CommandCall<UpdateInfoCommand, UpdateInfoCommand.Response>(this, mProcessHandler) {

            @Override
            protected UpdateInfoCommand setupCommand() {
                return new UpdateInfoCommand(getUserToken());
            }

            @Override
            protected void onCallResult(boolean success, UpdateInfoCommand.Response response) {
            }
        }.call();
    }

    public void modifyPassword(final String oldPassword, final String newPassword,
                               final NormalCallListener listener) {
        new CommandCall<ModifyPasswordCommand, ModifyPasswordCommand.Response>(
                this, mProcessHandler) {

            @Override
            protected ModifyPasswordCommand setupCommand() {
                ModifyPasswordCommand command = new ModifyPasswordCommand(getUserToken());
                command.putParamPassword(oldPassword, newPassword);
                return command;
            }

            @Override
            protected void onCallResult(boolean success, ModifyPasswordCommand.Response response) {
                notifyCallResult(listener, success);
            }
        }.call();
    }

    public void resetPassword(final String mobile, final String newPassword,
                              final String captcha, final NormalCallListener listener) {
        new CommandCall<ResetPasswordCommand, ResetPasswordCommand.Response>(
                this, mProcessHandler) {

            @Override
            protected ResetPasswordCommand setupCommand() {
                ResetPasswordCommand command = new ResetPasswordCommand();
                command.putParamMobile(mobile);
                command.putParamNewPassword(newPassword);
                command.putParamCaptcha(captcha);
                return command;
            }

            @Override
            protected void onCallResult(boolean success, ResetPasswordCommand.Response response) {
                notifyCallResult(listener, success);
            }
        }.call();
    }

    private void requestAvatarUrl(final String extName) {
        new CommandCall<RequestAvatarUrlCommand, RequestAvatarUrlCommand.Response>(
                this, mProcessHandler) {

            @Override
            protected RequestAvatarUrlCommand setupCommand() {
                RequestAvatarUrlCommand command = new RequestAvatarUrlCommand(getUserToken());
                command.putParamExtName(extName);
                return command;
            }

            @Override
            protected void onCallResult(boolean success,
                                        RequestAvatarUrlCommand.Response response) {
                // TODO
                if (success) {
                    response.getQiNiuKey();
                    response.getQiNiuToken();
                }
            }
        }.call();
    }

    public void thirdLogin(final String platform, final String openId,
                           final String accessToken, final String tokenExpire,
                           final NormalCallListener listener) {
        new CommandCall<ThirdLoginCommand, ThirdLoginCommand.Response>(this, mProcessHandler) {

            @Override
            protected ThirdLoginCommand setupCommand() {
                ThirdLoginCommand command = new ThirdLoginCommand();
                command.putParamPlatform(platform);
                command.putParamOpenId(openId);
                command.putParamAccessToken(accessToken);
                command.putParamTokenExpire(tokenExpire);
                return command;
            }

            @Override
            protected void onCallResult(boolean success, ThirdLoginCommand.Response response) {
                if (success) {
                    setUserToken(response.getToken());
                    setUserPassword(response.getPassword());
                    updateUserVO(response.getUserVO());
                }
                notifyCallResult(listener, success);
            }
        }.call();
    }

    public void bindMobile(final String platform, final String openId,
                           final String mobile, final String newPassword,
                           final String captcha, final NormalCallListener listener) {
        new CommandCall<BindMobileCommand, BindMobileCommand.Response>(this, mProcessHandler) {

            @Override
            protected BindMobileCommand setupCommand() {
                BindMobileCommand command = new BindMobileCommand(getUserToken());
                command.putParamPlatform(platform);
                command.putParamOpenId(openId);
                command.putParamMobile(mobile);
                command.putParamNewPassword(newPassword);
                command.putParamCaptcha(captcha);
                return command;
            }

            @Override
            protected void onCallResult(boolean success, BindMobileCommand.Response response) {
                notifyCallResult(listener, success);
            }
        }.call();
    }

    public boolean isLogin() {
        return !TextUtils.isEmpty(getUserToken()) && getUserVO() != null;
    }

    private void notifyCallResult(NormalCallListener listener, boolean success) {
        if (listener != null) {
            listener.onCallResult(success);
        }
    }
}
