
package com.ekuater.admaker.delegate.command;

import com.loopj.android.http.RequestHandle;

/**
 * @author LinYong
 */
public class HttpRequest extends AbstractRequest {

    private RequestHandle mRequestHandle;

    public HttpRequest(RequestHandle requestHandle) {
        mRequestHandle = requestHandle;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return mRequestHandle.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isFinished() {
        return mRequestHandle.isFinished();
    }

    @Override
    public boolean isCancelled() {
        return mRequestHandle.isCancelled();
    }
}
