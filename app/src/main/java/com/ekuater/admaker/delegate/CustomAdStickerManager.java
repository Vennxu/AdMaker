package com.ekuater.admaker.delegate;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.datastruct.TimeSticker;
import com.ekuater.admaker.util.BmpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Leo on 2015/6/9.
 *
 * @author LinYong
 */
final class CustomAdStickerManager {

    private static final int MAX_SIZE = 50;
    private static final String CUSTOM_FILE_NAME = "custom_ad_stickers";

    private volatile static CustomAdStickerManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new CustomAdStickerManager(context.getApplicationContext());
        }
    }

    static CustomAdStickerManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final List<TimeSticker> mStickerList;
    private final File mCustomFile;

    private CustomAdStickerManager(Context context) {
        mStickerList = new LinkedList<>();
        mCustomFile = context.getFileStreamPath(CUSTOM_FILE_NAME);
        loadIgnoreException();
    }

    public AdSticker addNewAdSticker(String title, AdSticker.Type type,
                                     Bitmap thumb, Bitmap image) {
        String id = AdStickerUtils.newLocalAdStickerId();
        String idContent = AdStickerUtils.getAdStickerIdContent(id);
        String thumbName = AdStickerUtils.getLocalAdStickerThumbFileName(idContent);
        String imageName = AdStickerUtils.getLocalAdStickerImageFileName(idContent);

        AdSticker sticker = new AdSticker(id, AdSticker.From.LOCAL, type,
                title, thumbName, imageName, null);
        BmpUtils.saveBitmapPng(thumb, AdStickerUtils.getLocalAdStickerFile(thumbName));
        BmpUtils.saveBitmapPng(image, AdStickerUtils.getLocalAdStickerFile(imageName));
        addToList(new TimeSticker(sticker, System.currentTimeMillis())); // save to local
        return sticker;
    }

    public AdSticker getAdSticker(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }

        AdSticker sticker = searchAdSticker(id);

        if (sticker == null) {
            String idContent = AdStickerUtils.getAdStickerIdContent(id);
            String thumbName = AdStickerUtils.getLocalAdStickerThumbFileName(idContent);
            String imageName = AdStickerUtils.getLocalAdStickerImageFileName(idContent);
            AdSticker tmpSticker = new AdSticker(id, AdSticker.From.LOCAL, null,
                    "", thumbName, imageName, null);
            sticker = AdStickerUtils.checkLocalAdSticker(tmpSticker) ? tmpSticker : null;
        }
        return sticker;
    }

    private AdSticker searchAdSticker(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }

        synchronized (mStickerList) {
            for (TimeSticker sticker : mStickerList) {
                if (id.equals(sticker.getId())) {
                    return sticker;
                }
            }
        }
        return null;
    }

    public TimeSticker[] getAdStickers() {
        TimeSticker[] stickers;
        List<TimeSticker> illegalList = new ArrayList<>();

        synchronized (mStickerList) {
            stickers = mStickerList.toArray(new TimeSticker[mStickerList.size()]);
        }

        for (TimeSticker sticker : stickers) {
            if (!AdStickerUtils.checkLocalAdSticker(sticker)) {
                illegalList.add(sticker);
            }
        }

        synchronized (mStickerList) {
            mStickerList.removeAll(illegalList);
            stickers = mStickerList.toArray(new TimeSticker[mStickerList.size()]);
        }
        saveIgnoreException();
        return stickers;
    }

    private void addToList(TimeSticker sticker) {
        TimeSticker removedSticker = null;

        synchronized (mStickerList) {
            final int size = mStickerList.size();
            if (size >= MAX_SIZE) {
                removedSticker = mStickerList.remove(size - 1);
            }
            mStickerList.add(0, sticker);
        }
        saveIgnoreException();

        // delete removed sticker
        if (removedSticker != null) {
            AdStickerUtils.deleteLocalAdSticker(removedSticker);
        }
    }

    private void saveIgnoreException() {
        try {
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() throws IOException {
        List<TimeSticker> list = new LinkedList<>();
        synchronized (mStickerList) {
            list.addAll(mStickerList);
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(mCustomFile, false);
            Gson gson = new Gson();
            gson.toJson(list, writer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void loadIgnoreException() {
        try {
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load() throws IOException {
        List<TimeSticker> list = null;
        FileReader reader = null;

        try {
            Gson gson = new Gson();
            reader = new FileReader(mCustomFile);
            list = gson.fromJson(
                    reader,
                    new TypeToken<LinkedList<TimeSticker>>() {
                    }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (list == null || list.size() <= 0) {
            return;
        }

        int size = list.size();

        if (size > MAX_SIZE) {
            for (TimeSticker sticker : list.subList(MAX_SIZE, size)) {
                AdStickerUtils.deleteLocalAdSticker(sticker);
            }
            list = list.subList(0, MAX_SIZE);
        }
        synchronized (mStickerList) {
            mStickerList.addAll(list);
        }
    }
}
