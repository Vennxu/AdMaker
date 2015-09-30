package com.ekuater.admaker.delegate;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ekuater.admaker.EnvConfig;
import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.util.UUIDGenerator;

import java.io.File;
import java.util.Locale;

/**
 * Created by Leo on 2015/6/9.
 *
 * @author LinYong
 */
public final class AdStickerUtils {

    private static final String INTERNAL = AdSticker.From.INTERNAL.name();
    private static final String LOCAL = AdSticker.From.LOCAL.name();
    private static final String ONLINE = AdSticker.From.ONLINE.name();

    @NonNull
    public static String genAdStickerId(@NonNull String type, @NonNull String id) {
        return String.format(Locale.ENGLISH, "%1$s:%2$s", type, id);
    }

    @NonNull
    public static String genInternalAdStickerId(@NonNull String id) {
        return genAdStickerId(INTERNAL, id);
    }

    @NonNull
    public static String genLocalAdStickerId(@NonNull String id) {
        return genAdStickerId(LOCAL, id);
    }

    @NonNull
    public static String genOnlineAdStickerId(@NonNull String id) {
        return genAdStickerId(ONLINE, id);
    }

    @NonNull
    public static String newLocalAdStickerId() {
        return genLocalAdStickerId(UUIDGenerator.generate());
    }

    @SuppressWarnings("unused")
    public static String getAdStickerIdType(@NonNull String id) {
        String[] values = id.split(":");
        return values.length >= 2 ? values[0] : null;
    }

    public static String getAdStickerIdContent(@NonNull String id) {
        String[] values = id.split(":");
        return values.length >= 2 ? values[1] : null;
    }

    public static String getLocalAdStickerThumbFileName(@NonNull String idContent) {
        return idContent + "_thumb.jpg";
    }

    public static String getLocalAdStickerImageFileName(@NonNull String idContent) {
        return idContent + ".png";
    }

    public static File getLocalAdStickerFile(String fileName) {
        return new File(EnvConfig.CUSTOM_STICKERS_DIR, fileName);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteLocalAdSticker(@NonNull AdSticker sticker) {
        File temp = getLocalAdStickerFile(sticker.getThumb());
        if (temp.exists()) {
            temp.delete();
        }
        temp = getLocalAdStickerFile(sticker.getImage());
        if (temp.exists()) {
            temp.delete();
        }
    }

    public static boolean checkLocalAdSticker(@NonNull AdSticker sticker) {
        if (TextUtils.isEmpty(sticker.getThumb())) {
            return false;
        }

        File temp = getLocalAdStickerFile(sticker.getThumb());
        if (!temp.exists()) {
            return false;
        }
        temp = getLocalAdStickerFile(sticker.getImage());
        return temp.exists();
    }
}
