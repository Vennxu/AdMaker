
package com.ekuater.admaker.command;

import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.datastruct.RequestCommand;
import com.ekuater.admaker.delegate.command.ICommandRequest;
import com.ekuater.admaker.util.JsonUtils;
import com.ekuater.admaker.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class BaseCommand {

    private static final String TAG = BaseCommand.class.getSimpleName();

    // Default HTTP request method
    public static final int DEFAULT_REQUEST = ConstantCode.REQUEST_POST;

    public static boolean isRunning(ICommandRequest cmdRequest) {
        return (cmdRequest != null && !cmdRequest.isFinished()
                && !cmdRequest.isCancelled());
    }

    private JSONObject mJsonParam;
    private JSONObject mJsonHeaders;
    private int mRequestMethod;
    private String mUrl;

    public BaseCommand() {
        mRequestMethod = DEFAULT_REQUEST;
        initParam();
    }

    private void initParam() {
        mJsonParam = new JSONObject();
        mJsonHeaders = new JSONObject();
        putBaseParameters();
    }

    public void setRequestMethod(int method) {
        mRequestMethod = method;
    }

    public int getRequestMethod() {
        return mRequestMethod;
    }

    public String getUrl() {
        return mUrl;
    }

    protected void setUrl(String url) {
        mUrl = url;
    }

    public final void addHeader(String header, String value) {
        try {
            mJsonHeaders.put(header, value);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    public final void putParam(String name, String value) {
        try {
            mJsonParam.put(name, value);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    public final void putParam(String name, JSONArray value) {
        try {
            mJsonParam.put(name, value);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    public final void putParam(String name, int value) {
        try {
            mJsonParam.put(name, value);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    public final void putParam(String name, long value) {
        try {
            mJsonParam.put(name, value);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    public final void putParam(String name, boolean value) {
        try {
            mJsonParam.put(name, value);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
    }

    public void clearParams() {
        initParam();
    }

    protected void putBaseParameters() {
    }

    public RequestCommand toRequestCommand() {
        RequestCommand request = new RequestCommand();
        request.setUrl(getUrl());
        request.setRequestMethod(getRequestMethod());
        request.setParam(mJsonParam.toString());
        request.setHeaders(mJsonHeaders.toString());
        return request;
    }

    @Override
    public String toString() {
        return ("RequestMethod=" + mRequestMethod)
                + (",Url=" + mUrl)
                + (",Param=" + mJsonParam.toString())
                + (",Headers=" + mJsonHeaders.toString());
    }

    @SuppressWarnings("unused")
    public static class Response {

        public static final String SUCCESS = "success";
        public static final String FAILURE = "failure";

        private int code;
        private String desc;
        private String state;

        public Response() {
        }

        public String getDesc() {
            return desc;
        }

        public int getCode() {
            return code;
        }

        public String getState() {
            return state;
        }

        public boolean executedSuccess() {
            switch (getState()) {
                case SUCCESS:
                    return true;
                case FAILURE:
                default:
                    return false;
            }
        }

        public boolean requestSuccess() {
            return executedSuccess() && (getCode() == CommandErrorCode.REQUEST_SUCCESS);
        }

        public boolean isUnauthorized() {
            return getCode() == CommandErrorCode.UNAUTHORIZED;
        }

        @Override
        public String toString() {
            return JsonUtils.toJson(this);
        }
    }
}
