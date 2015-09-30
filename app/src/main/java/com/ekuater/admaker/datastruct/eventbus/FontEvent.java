package com.ekuater.admaker.datastruct.eventbus;

/**
 * Created by Administrator on 2015/6/11.
 */
public class FontEvent {

    private String font;

    public FontEvent(String font){
        this.font = font;
    }

    public String getFont() {
        return font;
    }
}
