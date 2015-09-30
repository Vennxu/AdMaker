package com.ekuater.admaker.datastruct.eventbus;

import com.ekuater.admaker.datastruct.Term;

/**
 * Created by Administrator on 2015/6/11.
 */
public class ColorEvent {

    private int color;

    public ColorEvent(int color){
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
