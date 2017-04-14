package com.robotium.solo;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.View;
import com.robotium.solo.Solo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by xuanlubin on 2017/4/14.
 */
public class SoloExt extends Solo {
    public SoloExt(Instrumentation instrumentation, Activity activity) {
        super(instrumentation, activity);
    }

    public SoloExt(Instrumentation instrumentation, Config config) {
        super(instrumentation, config);
    }

    public SoloExt(Instrumentation instrumentation, Config config, Activity activity) {
        super(instrumentation, config, activity);
    }

    public SoloExt(Instrumentation instrumentation) {
        super(instrumentation);
    }

    public <T extends View> T getViewByDesc(String desc, Class<T> classFilterBy) {
        return getViewByDesc(desc, classFilterBy, 0);
    }


    public <T extends View> T getViewByDesc(String desc, Class<T> classFilterBy, int index) {
        return getViewByDesc(desc, classFilterBy, index, 0);
    }

    public <T extends View> T getViewByDesc(String desc, Class<T> classFilterBy, int index, int timeout) {
        return waitForViewDesc(desc, classFilterBy, index, timeout, true);
    }

    private <T extends View> T waitForViewDesc(String desc, Class<T> classFilterBy, int index, int timeout, boolean scroll) {
        long endTime = SystemClock.uptimeMillis() + timeout;
        while (SystemClock.uptimeMillis() <= endTime) {
            super.sleeper.sleep();
            ArrayList<T> list = viewFetcher.getCurrentViews(classFilterBy, true);
            if (null != list && !list.isEmpty()) {
                for (Iterator<T> iterator = list.iterator(); iterator.hasNext(); ) {
                    T view = iterator.next();
                    if (null == view.getContentDescription() || !view.getContentDescription().equals(desc)) {
                        iterator.remove();
                    }
                }
                if (!list.isEmpty() && list.size() > index) {
                    return list.get(index);
                }
            }
            if (scroll)
                scroller.scrollDown();
        }
        return null;
    }

    public <T extends View> T getView(Class<T> classFilterBy, View parent) {
        return getView(classFilterBy, parent, 0, 0, true);
    }

    public <T extends View> T getView(Class<T> classFilterBy, View parent, int index) {
        return getView(classFilterBy, parent, index, 0, true);
    }


    public <T extends View> T getView(Class<T> classFilterBy, View parent, int index, int timeout, boolean scroll) {
        long endTime = SystemClock.uptimeMillis() + timeout;
        while (SystemClock.uptimeMillis() <= endTime) {
            super.sleeper.sleep();
            ArrayList<T> list = viewFetcher.getCurrentViews(classFilterBy, true, parent);
            if (null != list && !list.isEmpty() && list.size() > index) {
                return list.get(index);
            }
            if (scroll)
                scroller.scrollDown();
        }
        return null;
    }
}
