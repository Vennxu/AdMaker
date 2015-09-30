package com.ekuater.admaker.ui.activity.base;

import android.app.Activity;

import java.util.Stack;

/**
 * Created by Leo on 2015/2/7.
 *
 * @author LinYong
 */
public class ActivityStack {

    private static ActivityStack ourInstance = new ActivityStack();

    public static ActivityStack getInstance() {
        return ourInstance;
    }

    private final Stack<Activity> stack;

    private ActivityStack() {
        stack = new Stack<Activity>();
    }

    public void addActivity(Activity activity) {
        synchronized (stack) {
            stack.add(activity);
        }
    }

    public void removeActivity(Activity activity) {
        synchronized (stack) {
            stack.remove(activity);
        }
    }

    public void finishAllActivity() {
        synchronized (stack) {
            while (stack.size() > 0) {
                stack.pop().finish();
            }
        }
    }
}
