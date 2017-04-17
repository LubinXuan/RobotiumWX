package me.robin.espressomodule;

import android.app.Activity;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by xuanlubin on 2017/4/17.
 */
@RunWith(AndroidJUnit4.class)
public class WxTestEspresso {
    public static final String TAG = "Espresso";
    private static Class<Activity> launchActivityClass;
    // 对应re-sign.jar生成出来的信息框里的两个值


    static {
        try {
            launchActivityClass = (Class<Activity>) Class.forName("com.tencent.mm.ui.LauncherUI");
            Log.i(TAG, "Class loaded :" + launchActivityClass.getClass());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public ActivityTestRule<Activity> mActivityRule = new ActivityTestRule<>(launchActivityClass);

    @Before
    public void setUp() {
        Log.i(TAG, "测试任务启动");
    }

    @Test
    public void postMoments() {
        // Type text and then press the button.
        onView(allOf(withText("发现"), isDisplayed())).perform(click());
        Log.i(TAG, "click 发现");
        onView(allOf(withText("朋友圈"), isDisplayed())).perform(click());
        Log.i(TAG, "click 朋友圈");
        onView(allOf(withContentDescription("更多功能按钮"), isDisplayed())).perform(longClick());
        Log.i(TAG, "click 更多功能按钮");
        onView(allOf(withHint("这一刻的想法..."), isDisplayed()))
                .perform(replaceText("中文测试 \r\npost by espresso!"))
                .perform(ViewActions.closeSoftKeyboard());
        Log.i(TAG, "type 更多功能按钮");
        onView(allOf(withText("发送"), isDisplayed())).perform(click());
        Log.i(TAG, "type 发送");
    }

}
