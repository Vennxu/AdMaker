package com.ekuater.admaker.command.account;

import com.ekuater.admaker.command.TokenCommand;
import com.ekuater.admaker.command.Utils;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
public class ModifyPasswordCommand extends TokenCommand {

    public static final String URL = "/services/user/modify_pass.json";

    public ModifyPasswordCommand(String token) {
        super(token);
        setUrl(URL);
    }

    public void putParamPassword(String oldPassword, String newPassword) {
        putParam("oldPassWord", Utils.encodePassword(oldPassword));
        putParam("newPassWord", Utils.encodePassword(newPassword));
    }
}
