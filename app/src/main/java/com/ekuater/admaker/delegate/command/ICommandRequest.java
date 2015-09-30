
package com.ekuater.admaker.delegate.command;

/**
 * @author LinYong
 */
public interface ICommandRequest {

    /**
     * Attempts to cancel this request.
     *
     * @param mayInterruptIfRunning true if the thread executing this request
     *                              should be interrupted; otherwise, in-progress requests are
     *                              allowed to complete
     * @return false if the request could not be cancelled, typically because it
     * has already completed normally; true otherwise
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * Returns true if this task completed. Completion may be due to normal
     * termination, an exception, or cancellation -- in all of these cases, this
     * method will return true.
     *
     * @return true if this task completed
     */
    boolean isFinished();

    /**
     * Returns true if this task was cancelled before it completed normally.
     *
     * @return true if this task was cancelled before it completed
     */
    boolean isCancelled();
}
