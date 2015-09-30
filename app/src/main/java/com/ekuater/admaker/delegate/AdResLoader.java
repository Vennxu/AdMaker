package com.ekuater.admaker.delegate;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ekuater.admaker.BuildConfig;
import com.ekuater.admaker.command.adres.AdCategoryCommand;
import com.ekuater.admaker.command.adres.AdCategoryItemCommand;
import com.ekuater.admaker.command.adres.AdSceneCommand;
import com.ekuater.admaker.command.adres.AdSloganCommand;
import com.ekuater.admaker.command.adres.AdTrademarkCommand;
import com.ekuater.admaker.command.adres.ResVersionCommand;
import com.ekuater.admaker.command.hotissue.LatestDaysHotIssueCommand;
import com.ekuater.admaker.datastruct.AdCategoryItem;
import com.ekuater.admaker.datastruct.AdCategoryItemVO;
import com.ekuater.admaker.datastruct.AdCategoryVO;
import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.datastruct.DayHotIssues;
import com.ekuater.admaker.datastruct.HotIssue;
import com.ekuater.admaker.datastruct.Scene;
import com.ekuater.admaker.datastruct.pojo.AdScene;
import com.ekuater.admaker.datastruct.pojo.AdSlogan;
import com.ekuater.admaker.datastruct.pojo.AdTrademark;
import com.ekuater.admaker.datastruct.pojo.ResVersion;
import com.ekuater.admaker.util.DateUtils;
import com.ekuater.admaker.util.TextUtil;

import org.afinal.simplecache.ACache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Leo on 2015/6/17.
 *
 * @author LinYong
 */
public class AdResLoader extends BaseManager {

    private static final String RES_VERSION_KEY = "res_version_key";
    private static final String RES_VER_UPDATE_TIME_KEY = "res_ver_update_time_key";
    private static final long RES_VER_UPDATE_DELAY = BuildConfig.DEBUG
            ? 30 * 1000 /* 30 seconds */ : 60 * 60 * 1000 /* one hour */;
    private static final int HOT_ISSUE_CACHE_EXPIRE_TIME = BuildConfig.DEBUG
            ? ACache.TIME_HOUR / 60 : ACache.TIME_HOUR;

    private volatile static AdResLoader sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new AdResLoader(context.getApplicationContext());
        }
    }

    public static AdResLoader getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private ACache mACache;
    private Handler mProcessHandler;
    private Map<String, ResVersion> mResVerMap;

    private AdResLoader(Context context) {
        super(context);
        mACache = ACache.get(context);
        mProcessHandler = new Handler(getProcessLooper());
        mResVerMap = new HashMap<>();

        mProcessHandler.post(new Runnable() {
            @Override
            public void run() {
                loadResVersions();
            }
        });
    }

    /**
     * Update resource version from server
     */
    @SuppressWarnings("unused")
    public void updateResVersion() {
        new CommandCall<ResVersionCommand, ResVersionCommand.Response>(this, mProcessHandler) {

            @Override
            protected boolean onPreSetupCommand() {
                return (System.currentTimeMillis() - getResUpdateTime() <= RES_VER_UPDATE_DELAY);
            }

            @Override
            protected ResVersionCommand setupCommand() {
                return new ResVersionCommand(ResVersion.AD_SOURCE);
            }

            @Override
            protected void onCallResult(boolean success, ResVersionCommand.Response response) {
                if (success) {
                    ResVersion[] versions = response.getResVersions();
                    if (versions != null) {
                        saveResVersions(versions);
                        saveResUpdateTime(System.currentTimeMillis());
                    }
                }
            }
        }.call();
    }

    private long getResUpdateTime() {
        try {
            return Long.parseLong(mACache.getAsString(RES_VER_UPDATE_TIME_KEY));
        } catch (Exception e) {
            return 0;
        }
    }

    private void saveResUpdateTime(long time) {
        try {
            mACache.put(RES_VER_UPDATE_TIME_KEY, String.valueOf(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveResVersions(ResVersion[] versions) {
        try {
            mACache.put(RES_VERSION_KEY, toJson(versions));
            updateResVersionMap(versions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadResVersions() {
        try {
            String value = mACache.getAsString(RES_VERSION_KEY);
            if (!TextUtil.isEmpty(value)) {
                updateResVersionMap(fromJson(value, ResVersion[].class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateResVersionMap(ResVersion[] versions) {
        mResVerMap.clear();
        for (ResVersion version : versions) {
            mResVerMap.put(version.getSource(), version);
        }
    }

    /**
     * Load Scene resources from server
     *
     * @param page     query page number, start from 1
     * @param listener load result listener
     */
    @SuppressWarnings("unused")
    public void loadScenes(final int page, @NonNull final AdResLoadListener<Scene> listener) {
        new CommandCall<AdSceneCommand, AdSceneCommand.Response>(this, mProcessHandler) {

            @Override
            protected boolean onPreSetupCommand() {
                ResCache cache = getRequestCache(AdSceneCommand.URL, page);
                if (cache != null && checkCacheValid(ResVersion.AD_SCENE, cache)) {
                    AdScene[] adScenes = fromJson(cache.getContent(), AdScene[].class);

                    if (adScenes != null && adScenes.length > 0) {
                        boolean remaining = adScenes.length >= AdSceneCommand.COUNT_PER_PAGE;
                        listener.onLoaded(true, remaining, AdResUtils.toScenes(adScenes));
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected AdSceneCommand setupCommand() {
                return new AdSceneCommand(page);
            }

            @Override
            protected void onCallResult(boolean success, AdSceneCommand.Response response) {
                if (success) {
                    AdScene[] adScenes = response.getScenes();
                    int count = adScenes != null ? adScenes.length : 0;
                    Scene[] scenes = adScenes != null ? AdResUtils.toScenes(adScenes) : null;
                    listener.onLoaded(true, count >= AdSceneCommand.COUNT_PER_PAGE, scenes);
                    // put request data to cache
                    putRequestCache(AdSceneCommand.URL, page,
                            newResCache(ResVersion.AD_SCENE, adScenes));
                } else {
                    listener.onLoaded(false, false, null);
                }
            }
        }.call();
    }

    /**
     * Load Slogan resources from server
     *
     * @param page     query page number, start from 1
     * @param listener load result listener
     */
    @SuppressWarnings("unused")
    public void loadSlogans(final int page, @NonNull final AdResLoadListener<AdSticker> listener) {
        new CommandCall<AdSloganCommand, AdSloganCommand.Response>(this, mProcessHandler) {

            @Override
            protected boolean onPreSetupCommand() {
                ResCache cache = getRequestCache(AdSloganCommand.URL, page);
                if (cache != null && checkCacheValid(ResVersion.AD_SLOGAN, cache)) {
                    AdSlogan[] slogans = fromJson(cache.getContent(), AdSlogan[].class);

                    if (slogans != null && slogans.length > 0) {
                        boolean remaining = slogans.length >= AdSloganCommand.COUNT_PER_PAGE;
                        listener.onLoaded(true, remaining, AdResUtils.toStickers(slogans));
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected AdSloganCommand setupCommand() {
                return new AdSloganCommand(page);
            }

            @Override
            protected void onCallResult(boolean success, AdSloganCommand.Response response) {
                if (success) {
                    AdSlogan[] slogans = response.getSlogans();
                    int count = slogans != null ? slogans.length : 0;
                    AdSticker[] stickers = slogans != null ? AdResUtils.toStickers(slogans) : null;
                    listener.onLoaded(true, count >= AdSloganCommand.COUNT_PER_PAGE, stickers);
                    // put request data to cache
                    putRequestCache(AdSloganCommand.URL, page,
                            newResCache(ResVersion.AD_SLOGAN, slogans));
                } else {
                    listener.onLoaded(false, false, null);
                }
            }
        }.call();
    }

    /**
     * Load Trademark resources from server
     *
     * @param page     query page number, start from 1
     * @param listener load result listener
     */
    @SuppressWarnings("unused")
    public void loadTrademarks(final int page,
                               @NonNull final AdResLoadListener<AdSticker> listener) {
        new CommandCall<AdTrademarkCommand, AdTrademarkCommand.Response>(this, mProcessHandler) {

            @Override
            protected boolean onPreSetupCommand() {
                ResCache cache = getRequestCache(AdTrademarkCommand.URL, page);
                if (cache != null && checkCacheValid(ResVersion.AD_TRADEMARK, cache)) {
                    AdTrademark[] trademarks = fromJson(cache.getContent(), AdTrademark[].class);

                    if (trademarks != null && trademarks.length > 0) {
                        boolean remaining = trademarks.length >= AdTrademarkCommand.COUNT_PER_PAGE;
                        listener.onLoaded(true, remaining, AdResUtils.toStickers(trademarks));
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected AdTrademarkCommand setupCommand() {
                return new AdTrademarkCommand(page);
            }

            @Override
            protected void onCallResult(boolean success, AdTrademarkCommand.Response response) {
                if (success) {
                    AdTrademark[] trademarks = response.getTrademarks();
                    int count = trademarks != null ? trademarks.length : 0;
                    AdSticker[] stickers = trademarks != null
                            ? AdResUtils.toStickers(trademarks) : null;
                    listener.onLoaded(true, count >= AdTrademarkCommand.COUNT_PER_PAGE, stickers);
                    // put request data to cache
                    putRequestCache(AdTrademarkCommand.URL, page,
                            newResCache(ResVersion.AD_TRADEMARK, trademarks));
                } else {
                    listener.onLoaded(false, false, null);
                }
            }
        }.call();
    }

    /**
     * Load category from server
     *
     * @param page     query page number, start from 1
     * @param listener load result listener
     */
    @SuppressWarnings("unused")
    public void loadCategories(final int page,
                               @NonNull
                               final AdResLoadListener<AdCategoryVO> listener) {
        new CommandCall<AdCategoryCommand, AdCategoryCommand.Response>(this, mProcessHandler) {

            @Override
            protected boolean onPreSetupCommand() {
                ResCache cache = getRequestCache(AdCategoryCommand.URL, page);
                if (cache != null && checkCacheValid(ResVersion.AD_CATEGORY, cache)) {
                    AdCategoryVO[] categories = fromJson(cache.getContent(), AdCategoryVO[].class);

                    if (categories != null && categories.length > 0) {
                        boolean remaining = categories.length >= AdCategoryCommand.COUNT_PER_PAGE;
                        listener.onLoaded(true, remaining, categories);
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected AdCategoryCommand setupCommand() {
                return new AdCategoryCommand(page);
            }

            @Override
            protected void onCallResult(boolean success,
                                        @Nullable
                                        AdCategoryCommand.Response response) {
                if (success) {
                    AdCategoryVO[] categories = response != null ? response.getCategories() : null;
                    int count = categories != null ? categories.length : 0;
                    listener.onLoaded(true, count >= AdCategoryCommand.COUNT_PER_PAGE, categories);
                    // put request data to cache
                    putRequestCache(AdCategoryCommand.URL, page,
                            newResCache(ResVersion.AD_CATEGORY, categories));
                } else {
                    listener.onLoaded(false, false, null);
                }
            }
        }.call();
    }

    /**
     * Load category items from server
     *
     * @param categoryId query category id
     * @param page       query page number, start from 1
     * @param listener   load result listener
     */
    @SuppressWarnings("unused")
    public void loadCategoryItems(final int categoryId, final int page,
                                  @NonNull
                                  final AdResLoadListener<AdCategoryItemVO> listener) {
        new CommandCall<AdCategoryItemCommand, AdCategoryItemCommand.Response>
                (this, mProcessHandler) {
            @Override
            protected boolean onPreSetupCommand() {
                ResCache cache = getRequestCache(
                        getIdUrl(AdCategoryItemCommand.URL, categoryId),
                        page);
                if (cache != null && checkCacheValid(ResVersion.AD_CATEGORY, cache)) {
                    AdCategoryItem[] items = fromJson(cache.getContent(), AdCategoryItem[].class);

                    if (items != null && items.length > 0) {
                        listener.onLoaded(true,
                                items.length >= AdCategoryItemCommand.COUNT_PER_PAGE,
                                AdResUtils.toCategoryItemVOs(items));
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected AdCategoryItemCommand setupCommand() {
                return new AdCategoryItemCommand(categoryId, page);
            }

            @Override
            protected void onCallResult(boolean success,
                                        @Nullable
                                        AdCategoryItemCommand.Response response) {
                if (success) {
                    AdCategoryItem[] items = response != null ? response.getCategoryItems() : null;
                    int count = items != null ? items.length : 0;
                    listener.onLoaded(true, count >= AdCategoryItemCommand.COUNT_PER_PAGE,
                            items != null ? AdResUtils.toCategoryItemVOs(items) : null);
                    putRequestCache(getIdUrl(AdCategoryItemCommand.URL, categoryId), page,
                            newResCache(ResVersion.AD_CATEGORY, items));
                } else {
                    listener.onLoaded(false, false, null);
                }
            }
        }.call();
    }

    /**
     * 获取最近几天热门事件资源
     *
     * @param days     最近天数
     * @param topCount 每天条数
     * @param page     query page number, start from 1
     * @param listener load result listener
     */
    public void loadLatestDaysHotIssues(final int days, final int topCount, final int page,
                                        @NonNull
                                        final AdResLoadListener<DayHotIssues> listener) {
        final String cacheKey = getLatestDaysHotIssuesKey(days, topCount, page);

        new CommandCall<LatestDaysHotIssueCommand, LatestDaysHotIssueCommand.Response>(
                this, mProcessHandler) {
            @Override
            protected boolean onPreSetupCommand() {
                final ResCache cache = getRequestCache(cacheKey);

                if (cache != null && DateUtils.isSameDay(cache.getTime(),
                        System.currentTimeMillis())) {
                    final HotIssue[] hotIssues = fromJson(cache.getContent(), HotIssue[].class);

                    if (hotIssues != null && hotIssues.length > 0) {
                        listener.onLoaded(true, hotIssues.length
                                        >= LatestDaysHotIssueCommand.COUNT_PER_PAGE,
                                assembleDayHotIssues(hotIssues));
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected LatestDaysHotIssueCommand setupCommand() {
                LatestDaysHotIssueCommand command = new LatestDaysHotIssueCommand();
                command.putParamDays(days);
                command.putParamTopCount(topCount);
                command.putParamPage(page);
                return command;
            }

            @Override
            protected void onCallResult(boolean success,
                                        @Nullable
                                        LatestDaysHotIssueCommand.Response response) {
                if (success && response != null) {
                    final HotIssue[] hotIssues = response.getHotIssues();
                    final int count = hotIssues != null ? hotIssues.length : 0;
                    listener.onLoaded(true, count >= LatestDaysHotIssueCommand.COUNT_PER_PAGE,
                            assembleDayHotIssues(hotIssues));

                    // 保存结果到缓存
                    ResCache cache = new ResCache();
                    cache.setTime(System.currentTimeMillis());
                    cache.setContent(toJson(hotIssues));
                    putRequestCache(cacheKey, cache, HOT_ISSUE_CACHE_EXPIRE_TIME);
                } else {
                    listener.onLoaded(false, false, null);
                }
            }
        }.call();
    }

    private String getLatestDaysHotIssuesKey(final int days, final int topCount, final int page) {
        return getExtraUrl(LatestDaysHotIssueCommand.URL,
                String.format(Locale.ENGLISH, "%1$d:%2$d:%3$d",
                        days, topCount, page));
    }

    private DayHotIssues[] assembleDayHotIssues(HotIssue[] hotIssues) {
        if (hotIssues != null && hotIssues.length > 0) {
            List<DayHotIssues> dayHotIssuesList = new ArrayList<>();
            DayHotIssues tempDayHotIssues = null;
            int tempStart = 0;
            int tempCount = 0;

            for (int i = 0; i < hotIssues.length; ++i) {
                HotIssue tempHotIssue = hotIssues[i];

                if (tempDayHotIssues == null || tempDayHotIssues.getDate()
                        != tempHotIssue.getDate()) {
                    if (tempDayHotIssues != null && tempCount > 0) {
                        HotIssue[] tempHotIssues = new HotIssue[tempCount];
                        System.arraycopy(hotIssues, tempStart, tempHotIssues, 0, tempCount);
                        tempDayHotIssues.setHotIssues(tempHotIssues);
                        dayHotIssuesList.add(tempDayHotIssues);
                    }

                    tempDayHotIssues = new DayHotIssues();
                    tempDayHotIssues.setDate(tempHotIssue.getDate());
                    tempStart = i;
                    tempCount = 0;
                }
                ++tempCount;
            }
            if (tempDayHotIssues != null && tempCount > 0) {
                HotIssue[] tempHotIssues = new HotIssue[tempCount];
                System.arraycopy(hotIssues, tempStart, tempHotIssues, 0, tempCount);
                tempDayHotIssues.setHotIssues(tempHotIssues);
                dayHotIssuesList.add(tempDayHotIssues);
            }

            return dayHotIssuesList.toArray(new DayHotIssues[dayHotIssuesList.size()]);
        } else {
            return new DayHotIssues[0];
        }
    }

    private ResCache getRequestCache(String url, int page) {
        return getRequestCache(getRequestCacheKey(url, page));
    }

    private void putRequestCache(String url, int page, ResCache cache) {
        putRequestCache(getRequestCacheKey(url, page), cache);
    }

    private ResCache getRequestCache(String key) {
        final String value = mACache.getAsString(key);
        return TextUtil.isEmpty(value) ? null : toResCache(value);
    }

    private void putRequestCache(String key, ResCache cache) {
        mACache.put(key, toString(cache));
    }

    private void putRequestCache(String key, ResCache cache, int saveTime) {
        mACache.put(key, toString(cache), saveTime);
    }

    private String getRequestCacheKey(String url, int page) {
        return String.format(Locale.ENGLISH, "%1$s:%2$d", url, page);
    }

    private String getIdUrl(String url, int id) {
        return getExtraUrl(url, String.valueOf(id));
    }

    private String getExtraUrl(String url, String extra) {
        return String.format(Locale.ENGLISH, "%1$s?%2$s", url, extra);
    }

    private ResCache toResCache(String cacheString) {
        return fromJson(cacheString, ResCache.class);
    }

    private String toString(ResCache cache) {
        return toJson(cache);
    }

    private boolean checkCacheValid(String source, ResCache cache) {
        ResVersion version = mResVerMap.get(source);
        return version != null
                && source.equals(cache.getSource())
                && (version.getVersion() == cache.getVersion())
                && !TextUtils.isEmpty(cache.getContent());
    }

    private ResCache newResCache(String source, Object content) {
        ResCache cache = new ResCache();
        ResVersion version = mResVerMap.get(source);

        cache.setSource(source);
        cache.setVersion(version != null ? version.getVersion() : 0);
        cache.setContent(toJson(content));
        return cache;
    }
}
