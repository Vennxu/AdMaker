package com.ekuater.admaker.delegate.command;

import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Leo on 2015/5/26.
 *
 * @author LinYong
 */
public class HttpClient extends AbstractClient {

    // private static final String TAG = "HttpClient";
    private static final String HEADER_ACCEPT = "accept";
    private static final String APPLICATION_JSON = "application/json";

    private volatile static HttpClient sSingleton;

    private static synchronized void initInstance() {
        if (sSingleton == null) {
            sSingleton = new HttpClient();
        }
    }

    public static HttpClient getInstance() {
        if (sSingleton == null) {
            initInstance();
        }
        return sSingleton;
    }

    private static final class LocalResponseHandler extends JsonHttpResponseHandler {

        private final ICommandResponse mResponse;

        public LocalResponseHandler(ICommandResponse response) {
            mResponse = response;
        }

        private int convertStatusCode(int statusCode) {
            return statusCode;
        }

        private String jsonToString(JSONObject json) {
            return (json != null) ? json.toString() : null;
        }

        private String jsonToString(JSONArray json) {
            return (json != null) ? json.toString() : null;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            mResponse.onSuccess(convertStatusCode(statusCode), jsonToString(response));
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            mResponse.onFailure(convertStatusCode(statusCode), response.toString(), new JSONException(
                    "JSONArray, unexpected response type," + response.toString()));
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                              JSONObject errorResponse) {
            mResponse.onFailure(convertStatusCode(statusCode), jsonToString(errorResponse), throwable);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                              JSONArray errorResponse) {
            mResponse.onFailure(convertStatusCode(statusCode), jsonToString(errorResponse), throwable);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString,
                              Throwable throwable) {
            mResponse.onFailure(convertStatusCode(statusCode), responseString, throwable);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            mResponse.onFailure(convertStatusCode(statusCode), responseString, new JSONException(
                    "Unexpected response type," + responseString));
        }
    }

    private final AsyncHttpClient mClient;

    private HttpClient() {
        mClient = new AsyncHttpClient();
        mClient.addHeader(HEADER_ACCEPT, APPLICATION_JSON);
    }

    @Override
    public ICommandRequest get(String url, String headers, String param,
                               ICommandResponse response) {
        LocalResponseHandler responseHandler = new LocalResponseHandler(response);
        RequestParams params = toRequestParams(param);
        RequestHandle requestHandle = mClient.get(null, url, toHeaders(headers),
                params, responseHandler);
        return new HttpRequest(requestHandle);
    }

    @Override
    public ICommandRequest post(String url, String headers, String param,
                                ICommandResponse response) {
        LocalResponseHandler responseHandler = new LocalResponseHandler(response);
        StringEntity entity = convertParam(param);
        RequestHandle requestHandle = mClient.post(null, url, toHeaders(headers),
                entity, null, responseHandler);
        return new HttpRequest(requestHandle);
    }

    private StringEntity convertParam(String param) {
        StringEntity entity = null;

        try {
            entity = new StringEntity(param, HTTP.UTF_8);
            entity.setContentType(APPLICATION_JSON);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return entity;
    }

    private RequestParams toRequestParams(String param) {
        try {
            JSONObject json = new JSONObject(param);
            Iterator<String> iterator = json.keys();
            RequestParams params = new RequestParams();

            params.setContentEncoding(HTTP.UTF_8);
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = json.optString(key);

                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    params.put(key, value);
                }
            }
            return params;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Header[] toHeaders(String headers) {
        try {
            List<Header> headerList = new ArrayList<>();
            JSONObject json = new JSONObject(headers);
            Iterator<String> iterator = json.keys();

            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = json.optString(key);

                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    headerList.add(new BasicHeader(key, value));
                }
            }
            return headerList.size() > 0 ? headerList.toArray(
                    new Header[headerList.size()]) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
