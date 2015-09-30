
package com.ekuater.admaker.delegate.command;

/**
 * command execute response interface
 *
 * @author LinYong
 */
public interface ICommandHandler {

    /**
     * on command execute response
     *
     * @param result   execute result, success or error code
     * @param response response String data
     */
    void onResponse(int result, String response);
}
