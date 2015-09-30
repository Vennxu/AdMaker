package com.ekuater.admaker.delegate;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.datastruct.RequestCommand;
import com.ekuater.admaker.delegate.command.ICommandHandler;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
interface ICommandClient {

    void executeCommand(BaseCommand command, ICommandHandler handler);

    void executeCommand(RequestCommand command, ICommandHandler handler);
}
