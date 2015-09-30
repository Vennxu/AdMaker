package com.ekuater.admaker.delegate;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.AdSticker;

/**
 * Created by Leo on 2015/6/1.
 *
 * @author LinYong
 */
final class AdStickerParser {

    private static final int IDX_TITLE = 0;
    private static final int IDX_THUMB = 1;
    private static final int IDX_IMAGE = 2;
    private static final int IDX_ALT_IMAGE = 3;
    private static final int FIELD_COUNT = 3;
    private static final int FIELD_ALT_COUNT = 4;

    public static AdSticker[] getInternalAdStickers(Resources res, AdSticker.Type type) {
        int resArray;

        switch (type) {
            case SLOGAN:
                resArray = R.array.slogan_stickers;
                break;
            case TRADEMARK:
                resArray = R.array.trademark_stickers;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return parseInternalAdStickers(res, type, resArray);
    }

    @Nullable
    public static AdSticker getInternalAdSticker(Resources res, String id) {
        try {
            return parseInternalAdSticker(res, null,
                    Integer.parseInt(AdStickerUtils.getAdStickerIdContent(id)));
        } catch (Exception e) {
            return null;
        }
    }

    private static AdSticker[] parseInternalAdStickers(Resources res, AdSticker.Type type,
                                                       int adStickerArray) {
        TypedArray ar = res.obtainTypedArray(adStickerArray);
        int length = ar.length();
        AdSticker[] stickers = new AdSticker[length];

        for (int i = 0; i < length; ++i) {
            stickers[i] = parseInternalAdSticker(res, type, ar.getResourceId(i, 0));
        }
        ar.recycle();
        return stickers;
    }

    private static AdSticker parseInternalAdSticker(Resources res, AdSticker.Type type,
                                                    int adStickerArrayId) {
        String[] fields = res.getStringArray(adStickerArrayId);

        if (fields.length != FIELD_COUNT && fields.length != FIELD_ALT_COUNT) {
            throw new IllegalArgumentException();
        }

        return new AdSticker(
                AdStickerUtils.genInternalAdStickerId(String.valueOf(adStickerArrayId)),
                AdSticker.From.INTERNAL,
                type,
                fields[IDX_TITLE],
                fields[IDX_THUMB],
                fields[IDX_IMAGE],
                fields.length == FIELD_ALT_COUNT ? fields[IDX_ALT_IMAGE] : null);
    }
}
