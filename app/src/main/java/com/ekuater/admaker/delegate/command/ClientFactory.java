
package com.ekuater.admaker.delegate.command;

/**
 * @author LinYong
 */
public final class ClientFactory {

    public static ICommandClient getDefaultClient() {
        return HttpClient.getInstance();
    }
}
