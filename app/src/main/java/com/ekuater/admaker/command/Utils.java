package com.ekuater.admaker.command;

import android.support.annotation.NonNull;

import com.ekuater.admaker.encoder.Base64Encoder;
import com.ekuater.admaker.encoder.MD5Encoder;

/**
 * Created by Leo on 2015/6/29.
 *
 * @author LinYong
 */
public final class Utils {

    // twice encrypt password by md5
    public static String encodePassword(String plainPassword) {
        MD5Encoder encoder = MD5Encoder.getInstance();
        String cipherPassword = encoder.encode(encoder.encode(plainPassword));
        return (cipherPassword == null) ? plainPassword : cipherPassword;
    }

    public static String encodeToken(@NonNull String token) {
        return Base64Encoder.getInstance().encode(token);
    }
}
