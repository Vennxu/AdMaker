
package com.ekuater.admaker.delegate.command;

/**
 * @author LinYong
 */
public interface ICommandResponse {

    void onSuccess(int statusCode, String response);

    void onFailure(int statusCode, String response, Throwable throwable);
}
