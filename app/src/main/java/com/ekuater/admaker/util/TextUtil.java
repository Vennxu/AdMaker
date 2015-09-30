package com.ekuater.admaker.util;

/**
 * Created by Leo on 2015/4/25.
 *
 * @author LinYong
 */
public class TextUtil {

    private static final char[] ELLIPSIS_NORMAL = {'\u2026'}; // this is "..."
    private static final String ELLIPSIS_STRING = new String(ELLIPSIS_NORMAL);

    public static boolean isEmpty(CharSequence str) {
        return (str == null || str.length() == 0);
    }

    public static String ellipsize(String text, int maxLength) {
        if (isEmpty(text) || maxLength <= 0) {
            return text;
        }

        int length = text.length();
        if (maxLength < length) {
            return text.substring(0, maxLength - 1) + ELLIPSIS_STRING;
        } else {
            return text;
        }
    }
}
