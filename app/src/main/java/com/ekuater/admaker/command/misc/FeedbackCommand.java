package com.ekuater.admaker.command.misc;

import com.ekuater.admaker.command.BaseCommand;

/**
 * Created by Leo on 2015/7/19.
 *
 * @author LinYong
 */
public class FeedbackCommand extends BaseCommand {

    public static final String URL = "/services/feedback/create";

    public FeedbackCommand() {
        super();
        setUrl(URL);
    }

    public void putParamUserId(String userId) {
        putParam("userId", userId);
    }

    public void putParamAdMakerCode(String adMakerCode) {
        putParam("admakerCode", adMakerCode);
    }

    public void putParamNickname(String nickname) {
        putParam("nickName", nickname);
    }

    public void putParamSuggestion(String suggestion) {
        putParam("suggestion", suggestion);
    }

    public void putParamContact(String contact) {
        putParam("contact", contact);
    }
}
