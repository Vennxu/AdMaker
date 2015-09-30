package com.ekuater.admaker.command;

import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
public abstract class TokenCommand extends BaseCommand {

    public TokenCommand(@NonNull String token) {
        super();
        addHeader("Authorization",
                String.format(Locale.ENGLISH, "Bearer %1$s",
                        Utils.encodeToken(token)));
    }
}
