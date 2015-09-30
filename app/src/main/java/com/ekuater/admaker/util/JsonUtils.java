package com.ekuater.admaker.util;

import com.google.gson.Gson;

/**
 * Created by Leo on 2015/7/2.
 *
 * @author LinYong
 */
public final class JsonUtils {

    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return new Gson().fromJson(json, classOfT);
        } catch (Exception e) {
            return null;
        }
    }

    public static String toJson(Object src) {
        try {
            return new Gson().toJson(src);
        } catch (Exception e) {
            return null;
        }
    }
}
