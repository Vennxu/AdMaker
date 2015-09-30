package com.ekuater.admaker.command.account;

import com.ekuater.admaker.command.TokenCommand;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
public class UpdateInfoCommand extends TokenCommand {

    public static final String URL = "/services/user/update_info.json";

    public UpdateInfoCommand(String token) {
        super(token);
        setUrl(URL);
    }

    public void putParamNickname(String nickname) {
        putParam("nickName", nickname);
    }

    public void putParamGender(int gender) {
        putParam("sex", gender);
    }

    public void putParamAddress(String address) {
        putParam("address", address);
    }

    public void putParamConstellation(int constellation) {
        putParam("constellation", constellation);
    }

    public void putParamSchool(String school) {
        putParam("school", school);
    }

    public void putParamSignature(String signature) {
        putParam("signature", signature);
    }
}
