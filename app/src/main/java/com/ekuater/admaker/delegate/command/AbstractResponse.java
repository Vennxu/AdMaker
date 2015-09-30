
package com.ekuater.admaker.delegate.command;

/**
 * @author LinYong
 */
public class AbstractResponse implements ICommandResponse {

    @Override
    public void onSuccess(int statusCode, String response) {
    }

    @Override
    public void onFailure(int statusCode, String response, Throwable throwable) {
    }
}
