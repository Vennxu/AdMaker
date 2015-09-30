package com.ekuater.admaker.delegate;

import android.content.Context;

import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.datastruct.TimeSticker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Leo on 2015/6/3.
 *
 * @author LinYong
 */
class RecentAdStickers {

    private static final String RECENT_FILE_NAME = "RecentAdStickers";
    private static final int MAX_SIZE = 10;

    private volatile static RecentAdStickers sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new RecentAdStickers(context.getApplicationContext());
        }
    }

    static RecentAdStickers getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final List<TimeSticker> mStickerList = new ArrayList<>();
    private final File mRecentFile;

    private RecentAdStickers(Context context) {
        mRecentFile = context.getFileStreamPath(RECENT_FILE_NAME);
        mStickerList.clear();
        loadIgnoreException();
    }

    private void loadIgnoreException() {
        try {
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load recent AdSticker ids from pref
     */
    private void load() throws IOException {
        List<TimeSticker> list = null;
        FileReader reader = null;

        try {
            Gson gson = new Gson();
            reader = new FileReader(mRecentFile);
            list = gson.fromJson(
                    reader,
                    new TypeToken<ArrayList<TimeSticker>>() {
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

        List<TimeSticker> addList = new ArrayList<>();
        for (int i = 0, count = 0, length = list.size(); i < length && count <= MAX_SIZE; ++i) {
            TimeSticker sticker = list.get(i);
            if (sticker != null && sticker.isLegal()) {
                addList.add(sticker);
                ++count;
            }
        }
        synchronized (mStickerList) {
            mStickerList.addAll(addList);
        }
    }

    private void saveIgnoreException() {
        try {
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save recent AdSticker ids to pref
     */
    private void save() throws IOException {
        List<AdSticker> list = new ArrayList<>();
        synchronized (mStickerList) {
            list.addAll(mStickerList);
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(mRecentFile, false);
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

    /**
     * Put a new AdSticker to recent list
     *
     * @param sticker AdSticker
     */
    public void addAdSticker(AdSticker sticker) {
        addAdSticker(new TimeSticker(sticker, System.currentTimeMillis()));
    }

    public void addAdSticker(TimeSticker sticker) {
        synchronized (mStickerList) {
            Iterator<TimeSticker> iterator = mStickerList.iterator();
            while (iterator.hasNext()) {
                TimeSticker tmpSticker = iterator.next();
                if (sticker.getId().equals(tmpSticker.getId())) {
                    iterator.remove();
                }
            }

            int size = mStickerList.size();
            if (size >= MAX_SIZE) {
                mStickerList.remove(size - 1);
            }
            mStickerList.add(0, sticker);
        }
        saveIgnoreException();
    }

    /**
     * Load all recent AdStickers
     *
     * @return all recent AdSticker ids
     */
    public TimeSticker[] getAdStickers() {
        TimeSticker[] stickers;

        synchronized (mStickerList) {
            stickers = mStickerList.toArray(new TimeSticker[mStickerList.size()]);
        }
        return stickers;
    }
}
