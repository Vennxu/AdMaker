
package com.ekuater.admaker.util;

import android.text.TextUtils;

/**
 * UniqueFileName class use to generate unique file name
 * 
 * @author LinYong
 */
public final class UniqueFileName {

    /**
     * Get unique file name
     * 
     * @return unique file name
     */
    public static String getUniqueFileName() {
        return UUIDGenerator.generate();
    }

    /**
     * Get unique file name witch file name extension
     * 
     * @param extension file name extension
     * @return unique file name
     */
    public static String getUniqueFileName(String extension) {
        StringBuilder sb = new StringBuilder();

        sb.append(UUIDGenerator.generate());
        if (!TextUtils.isEmpty(extension)) {
            sb.append(".");
            sb.append(extension);
        }

        return sb.toString();
    }
}
