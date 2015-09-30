package com.ekuater.admaker.delegate;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/7/8.
 *
 * @author LinYong
 */
public class EventBusHub {

    private static final EventBus sDefaultEventBus = new EventBus();

    public static EventBus getDefaultEventBus() {
        return sDefaultEventBus;
    }
}
