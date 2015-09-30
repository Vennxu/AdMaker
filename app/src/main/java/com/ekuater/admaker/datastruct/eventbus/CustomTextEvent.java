package com.ekuater.admaker.datastruct.eventbus;

/**
 * Created by Administrator on 2015/6/11.
 */
public class CustomTextEvent {

    private String content;

    public CustomTextEvent(String content){
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
