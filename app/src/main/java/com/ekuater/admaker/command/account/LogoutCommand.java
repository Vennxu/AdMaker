package com.ekuater.admaker.command.account;

import com.ekuater.admaker.command.TokenCommand;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
public class LogoutCommand extends TokenCommand {

    public static final String URL = "/services/user/logout.json";

    public LogoutCommand(String token) {
        super(token);
        setUrl(URL);
    }
}
