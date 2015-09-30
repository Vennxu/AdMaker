package com.ekuater.admaker.delegate;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.datastruct.TimeSticker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Leo on 2015/6/10.
 *
 * @author LinYong
 */
public class AdStickerManager {

    private volatile static AdStickerManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new AdStickerManager(context.getApplicationContext());
        }
    }

    public static AdStickerManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private Context mContext;
    private CustomAdStickerManager mCustomManager;
    private RecentAdStickers mRecentManager;

    private AdStickerManager(Context context) {
        mContext = context;
        mCustomManager = CustomAdStickerManager.getInstance(context);
        mRecentManager = RecentAdStickers.getInstance(context);
    }

    public AdSticker[] getInternalAdStickers(AdSticker.Type type) {
        return AdStickerParser.getInternalAdStickers(mContext.getResources(), type);
    }

    public AdSticker addNewCustomAdSticker(String title, AdSticker.Type type,
                                           Bitmap thumb, Bitmap image) {
        return mCustomManager.addNewAdSticker(title, type, thumb, image);
    }

    public AdSticker[] getCustomAdStickers() {
        return mCustomManager.getAdStickers();
    }

    public void addRecentAdSticker(@NonNull AdSticker sticker) {
        mRecentManager.addAdSticker(sticker);
    }

    public AdSticker[] getRecentAdStickers() {
        return mRecentManager.getAdStickers();
    }

    public AdSticker[] getRecentAndCustomStickers() {
        List<TimeSticker> customList = new ArrayList<>(
                Arrays.asList(mCustomManager.getAdStickers()));
        List<TimeSticker> recentList = new ArrayList<>(
                Arrays.asList(mRecentManager.getAdStickers()));
        List<TimeSticker> integrateList = new ArrayList<>(customList);

        // integrate recent and custom list
        integrateList.removeAll(recentList);
        integrateList.addAll(recentList);

        // sort as time
        Collections.sort(integrateList, new Comparator<TimeSticker>() {
            @Override
            public int compare(TimeSticker lhs, TimeSticker rhs) {
                long diff = rhs.getTime() - lhs.getTime();
                return diff > 0 ? 1 : (diff == 0 ? 0 : -1);
            }
        });
        return integrateList.toArray(new TimeSticker[integrateList.size()]);
    }
}
