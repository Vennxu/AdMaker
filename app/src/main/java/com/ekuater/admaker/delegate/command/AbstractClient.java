
package com.ekuater.admaker.delegate.command;

/**
 * @author LinYong
 */
public class AbstractClient implements ICommandClient {

    @Override
    public ICommandRequest get(String url, String headers, String param,
                               ICommandResponse response) {
        return null;
    }

    @Override
    public ICommandRequest post(String url, String headers, String param,
                                ICommandResponse response) {
        return null;
    }

    @Override
    public ICommandRequest put(String url, String headers, String param,
                               ICommandResponse response) {
        return null;
    }

    @Override
    public ICommandRequest delete(String url, String headers, String param,
                                  ICommandResponse response) {
        return null;
    }
}
