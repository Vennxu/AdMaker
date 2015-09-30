
package com.ekuater.admaker.delegate.command;

/**
 * @author LinYong
 */
public class AbstractRequest implements ICommandRequest {

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

}
