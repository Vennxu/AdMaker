
package com.ekuater.admaker.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

import com.ekuater.admaker.util.UUIDGenerator;

/**
 * @author LinYong
 */
public class RequestCommand implements Parcelable {

    private static final String EMPTY_STRING = "";

    private String mSession;
    private String mUrl;
    private String mParam;
    private String mHeaders;
    private int mRequestMethod;

    public RequestCommand() {
        mSession = UUIDGenerator.generate();
        mUrl = EMPTY_STRING;
        mParam = EMPTY_STRING;
        mRequestMethod = ConstantCode.REQUEST_POST;
    }

    protected RequestCommand(Parcel source) {
        mSession = source.readString();
        mUrl = source.readString();
        mParam = source.readString();
        mHeaders = source.readString();
        mRequestMethod = source.readInt();
    }

    public String getSession() {
        return mSession;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = (url != null) ? url : EMPTY_STRING;
    }

    public String getParam() {
        return mParam;
    }

    public void setParam(String param) {
        mParam = (param != null) ? param : EMPTY_STRING;
    }

    public String getHeaders() {
        return mHeaders;
    }

    public void setHeaders(String headers) {
        mHeaders = (headers != null) ? headers : EMPTY_STRING;
    }

    public int getRequestMethod() {
        return mRequestMethod;
    }

    public void setRequestMethod(int method) {
        mRequestMethod = method;
    }

    @Override
    public String toString() {
        return "session=" + getSession()
                + ", requestMethod=" + getRequestMethod()
                + ", url=" + getUrl()
                + ", param=" + getParam()
                + ", headers=" + getHeaders();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSession);
        dest.writeString(mUrl);
        dest.writeString(mParam);
        dest.writeString(mHeaders);
        dest.writeInt(mRequestMethod);
    }

    public static final Parcelable.Creator<RequestCommand> CREATOR
            = new Parcelable.Creator<RequestCommand>() {

        @Override
        public RequestCommand createFromParcel(Parcel source) {
            return new RequestCommand(source);
        }

        @Override
        public RequestCommand[] newArray(int size) {
            return new RequestCommand[size];
        }
    };
}
