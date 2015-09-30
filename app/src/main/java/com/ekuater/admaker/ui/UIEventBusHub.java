package com.ekuater.admaker.ui;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/2/3.
 * @author LinYong
 */
public class UIEventBusHub {

    private static final EventBus sDefaultEventBus = new EventBus();

    public static EventBus getDefaultEventBus() {
        return sDefaultEventBus;
    }
}
