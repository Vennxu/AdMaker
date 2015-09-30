
package com.ekuater.admaker.delegate.command;

/**
 * @author LinYong
 */
public interface ICommandClient {

    ICommandRequest get(String url, String headers, String param, ICommandResponse response);

    ICommandRequest post(String url, String headers, String param, ICommandResponse response);

    ICommandRequest put(String url, String headers, String param, ICommandResponse response);

    ICommandRequest delete(String url, String headers, String param, ICommandResponse response);
}
