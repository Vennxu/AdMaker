package com.ekuater.admaker.delegate;

import android.graphics.Point;
import android.support.annotation.NonNull;

import com.ekuater.admaker.datastruct.AdCategoryItem;
import com.ekuater.admaker.datastruct.AdCategoryItemVO;
import com.ekuater.admaker.datastruct.AdSticker;
import com.ekuater.admaker.datastruct.Scene;
import com.ekuater.admaker.datastruct.pojo.AdScene;
import com.ekuater.admaker.datastruct.pojo.AdSlogan;
import com.ekuater.admaker.datastruct.pojo.AdTrademark;

/**
 * Created by Leo on 2015/6/18.
 *
 * @author LinYong
 */
public final class AdResUtils {

    public static Point toPoint(String string) {
        try {
            String[] values = string.split(",");
            return new Point(Integer.parseInt(values[0]),
                    Integer.parseInt(values[1]));
        } catch (Exception e) {
            return new Point();
        }
    }

    public static Scene toScene(@NonNull AdScene adScene) {
        Scene scene = new Scene();

        scene.setImage(adScene.getSceneImg());
        scene.setImageThumb(adScene.getSceneThumbImg());
        scene.setLeftTop(toPoint(adScene.getLeftTop()));
        scene.setLeftBottom(toPoint(adScene.getLeftBottom()));
        scene.setRightTop(toPoint(adScene.getRightTop()));
        scene.setRightBottom(toPoint(adScene.getRightBottom()));
        scene.setContentSize(toPoint(adScene.getContentSize()));
        return scene;
    }

    public static Scene[] toScenes(@NonNull AdScene[] adScenes) {
        Scene[] scenes = new Scene[adScenes.length];
        for (int i = 0; i < adScenes.length; ++i) {
            scenes[i] = toScene(adScenes[i]);
        }
        return scenes;
    }

    public static AdSticker toSticker(@NonNull AdSlogan slogan) {
        return new AdSticker(
                AdStickerUtils.genOnlineAdStickerId(String.valueOf(slogan.getId())),
                AdSticker.From.ONLINE,
                AdSticker.Type.SLOGAN,
                null,
                null,
                slogan.getHorzWordImg(),
                slogan.getVertWordImg());
    }

    public static AdSticker[] toStickers(@NonNull AdSlogan[] slogans) {
        AdSticker[] stickers = new AdSticker[slogans.length];
        for (int i = 0; i < slogans.length; ++i) {
            stickers[i] = toSticker(slogans[i]);
        }
        return stickers;
    }

    public static AdSticker toSticker(@NonNull AdTrademark trademark) {
        return new AdSticker(
                AdStickerUtils.genOnlineAdStickerId(String.valueOf(trademark.getId())),
                AdSticker.From.ONLINE,
                AdSticker.Type.TRADEMARK,
                null,
                null,
                trademark.getBrandImg(),
                null);
    }

    public static AdSticker[] toStickers(@NonNull AdTrademark[] trademarks) {
        AdSticker[] stickers = new AdSticker[trademarks.length];
        for (int i = 0; i < trademarks.length; ++i) {
            stickers[i] = toSticker(trademarks[i]);
        }
        return stickers;
    }

    public static AdCategoryItemVO toCategoryItemVO(@NonNull AdCategoryItem item) {
        AdCategoryItemVO itemVO = new AdCategoryItemVO();
        itemVO.setItemId(item.getItemId());
        itemVO.setCategoryId(item.getCategoryId());
        itemVO.setTrademark(toSticker(item.getTrademark()));
        itemVO.setSlogan(toSticker(item.getSlogan()));
        return itemVO;
    }

    public static AdCategoryItemVO[] toCategoryItemVOs(@NonNull AdCategoryItem[] items) {
        AdCategoryItemVO[] itemVOs = new AdCategoryItemVO[items.length];
        for (int i = 0; i < items.length; ++i) {
            itemVOs[i] = toCategoryItemVO(items[i]);
        }
        return itemVOs;
    }
}
