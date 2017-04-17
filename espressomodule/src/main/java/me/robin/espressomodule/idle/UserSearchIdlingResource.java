package me.robin.espressomodule.idle;

import android.app.Activity;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.util.Log;
import me.robin.espressomodule.WxTestEspresso;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xuanlubin on 2017/4/17.
 */
public class UserSearchIdlingResource implements IdlingResource {

    private final ActivityTestRule<Activity> testRule;

    private Activity currentActivity;

    private ResourceCallback resourceCallback;

    public UserSearchIdlingResource(ActivityTestRule<Activity> testRule) {
        this.testRule = testRule;
    }

    @Override
    public String getName() {
        return UserSearchIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        Activity activity = getActivityInstance();
        if (null != this.resourceCallback && null != activity) {
            String className = activity.getClass().getName();
            if ("com.tencent.mm.plugin.profile.ui.ContactInfoUI".equals(className)) {
                Log.i(WxTestEspresso.TAG, "检索完成,找到目标");
                this.resourceCallback.onTransitionToIdle();
                return true;
            }
            if (className.startsWith("com.tencent.mm.ui.chatting")) {
                Log.i(WxTestEspresso.TAG, "检索完成,找到目标");
                this.resourceCallback.onTransitionToIdle();
                return true;
            }
        }
        return false;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.resourceCallback = callback;
    }

    private Activity getActivityInstance() {
        Collection<Activity> resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
        for (Activity act : resumedActivities) {
            Log.d(WxTestEspresso.TAG, "THREAD:" + Thread.currentThread() + " Your current activity: " + act.getClass().getName());
            return act;
        }
        return null;
    }
}
