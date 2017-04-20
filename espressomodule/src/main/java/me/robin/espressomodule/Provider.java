package me.robin.espressomodule;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;

/**
 * Created by xuanlubin on 2017/4/20.
 */
public class Provider {
    private String logTag;
    private Instrumentation instrumentation;
    private UiDevice uiDevice;
    private ActivityTestRule<Activity> rule;

    public Provider(String logTag, Instrumentation instrumentation, UiDevice uiDevice, ActivityTestRule<Activity> rule) {
        this.logTag = logTag;
        this.instrumentation = instrumentation;
        this.uiDevice = uiDevice;
        this.rule = rule;
    }

    public String getLogTag() {
        return logTag;
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public UiDevice getUiDevice() {
        return uiDevice;
    }

    public ActivityTestRule<Activity> getRule() {
        return rule;
    }
}
