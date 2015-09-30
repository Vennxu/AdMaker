package com.ekuater.admaker.delegate;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.datastruct.RequestCommand;
import com.ekuater.admaker.datastruct.UserVO;
import com.ekuater.admaker.delegate.command.CommandExecutor;
import com.ekuater.admaker.delegate.command.ICommandHandler;
import com.ekuater.admaker.settings.Settings;
import com.ekuater.admaker.util.JsonUtils;
import com.ekuater.admaker.util.L;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONObject;

import java.io.File;

/**
 * @author LinYong
 */
/*package*/abstract class BaseManager implements ICommandClient {

    private static final String TAG = BaseManager.class.getSimpleName();

    private static final String USER_TOKEN_KEY = "UserToken";
    private static final String USER_PASSWORD_KEY = "UserPassword";
    private static final String USER_VO_KEY = "UserVO";

    private volatile static boolean sInit = false;
    private static Looper sLooper;

    private static synchronized void staticInitInternal() {
        if (!sInit) {
            sInit = true;
            HandlerThread thread = new HandlerThread("manager_thread",
                    Process.THREAD_PRIORITY_DEFAULT);
            thread.start();
            sLooper = thread.getLooper();
        }
    }

    private static void staticInit() {
        if (!sInit) {
            staticInitInternal();
        }
    }

    private CommandExecutor mCommandExecutor;
    private ContentResolver mCR;
    private UploadManager mUploadManager;

    protected BaseManager(Context context) {
        staticInit();
        mCommandExecutor = CommandExecutor.getInstance(context);
        mCR = context.getContentResolver();
        mUploadManager = new UploadManager();
    }

    @Override
    public void executeCommand(BaseCommand command, ICommandHandler handler) {
        executeCommand(command.toRequestCommand(), handler);
    }

    @Override
    public void executeCommand(RequestCommand command, ICommandHandler handler) {
        mCommandExecutor.execute(command, handler);
    }

    protected Looper getProcessLooper() {
        return sLooper;
    }

    protected <T> T fromJson(String json, Class<T> classOfT) {
        return JsonUtils.fromJson(json, classOfT);
    }

    protected String toJson(Object src) {
        return JsonUtils.toJson(src);
    }

    @Nullable
    public UserVO getUserVO() {
        String value = Settings.Personal.getString(mCR, USER_VO_KEY);
        return TextUtils.isEmpty(value) ? null : fromJson(value, UserVO.class);
    }

    @Nullable
    public String getUserId() {
        UserVO userVO = getUserVO();
        return userVO != null ? userVO.getUserId() : null;
    }

    protected void updateUserVO(UserVO userVO) {
        String value = userVO != null ? toJson(userVO) : null;
        Settings.Personal.putString(mCR, USER_VO_KEY, value);
    }

    protected String getUserToken() {
        return checkNull(Settings.Personal.getString(mCR, USER_TOKEN_KEY));
    }

    protected void setUserToken(String token) {
        Settings.Personal.putString(mCR, USER_TOKEN_KEY, checkNull(token));
    }

    protected String getUserPassword() {
        return checkNull(Settings.Personal.getString(mCR, USER_PASSWORD_KEY));
    }

    protected void setUserPassword(String password) {
        Settings.Personal.putString(mCR, USER_PASSWORD_KEY, checkNull(password));
    }

    protected void uploadFileToQiNiu(@NonNull final File file,
                                     @NonNull final String key,
                                     @NonNull final String token,
                                     @NonNull final UploadListener listener) {
        UpCompletionHandler handler = new UpCompletionHandler() {

            @Override
            public void complete(String key, final ResponseInfo info, final JSONObject response) {
                new Handler(getProcessLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        boolean success = info != null && info.isOK();
                        String resp = response != null ? response.toString() : null;
                        L.v(TAG, "uploadFileToQiNiu(), complete, response=" + resp);
                        listener.onComplete(success, resp);
                    }
                });
            }
        };
        UpProgressHandler progressHandler = new UpProgressHandler() {
            @Override
            public void progress(String key, double percent) {
                listener.onProgress(percent);
            }
        };
        UploadOptions options = new UploadOptions(null, null, false, progressHandler, null);
        mUploadManager.put(file, key, token, handler, options);
    }

    protected String checkNull(String string) {
        return string != null ? string : "";
    }
}
