package me.robin.espressomodule;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.FailureHandler;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.*;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import me.robin.espressomodule.idle.UserSearchIdlingResource;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

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
    private List<IdlingResource> idlingResourceList = new ArrayList<>(32);

    UiDevice device;

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
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void postMoments() {
        // Type text and then press the button.
        onView(allOf(withText("发现"), isDisplayed())).perform(click());
        onView(allOf(withText("朋友圈"), isDisplayed())).perform(click());
        onView(allOf(withContentDescription("更多功能按钮"), isDisplayed())).perform(longClick());
        onView(allOf(withHint("这一刻的想法..."), isDisplayed()))
                .perform(replaceText("中文测试 \r\npost by espresso!"));
        onView(allOf(withText("发送"), isDisplayed())).perform(click());
    }

    @Test
    public void sendMessage() throws Exception {
        int messages = 10;
        IdlingResource idlingResource = new UserSearchIdlingResource(mActivityRule);
        for (int i = 0; i < messages; i++) {
            Log.i(TAG, "THREAD:" + Thread.currentThread());
            String searchText = "420027600";
            onView(allOf(withContentDescription("更多功能按钮"), isDisplayed())).perform(click());
            onView(allOf(withText("添加朋友"), isDisplayed())).perform(click());
            onView(allOf(withText("微信号/QQ号/手机号"), isDisplayed())).perform(click());
            onView(allOf(withHint("搜索"), isDisplayed())).perform(replaceText(searchText));


            onView(allOf(withText("搜索:" + searchText), isDisplayed())).perform(click());
            Espresso.registerIdlingResources(idlingResource);
            _sendMessage(idlingResource, i);
            Thread.sleep(1000);
        }
    }

    private void _sendMessage(IdlingResource idlingResource, int i) throws UiObjectNotFoundException {
        //onView(allOf(withText("发消息"), isDisplayed())).perform(click());

        UiObject openChat = device.findObject(new UiSelector().text("发消息"));

        if (openChat.exists()) {
            openChat.click();
        }

        onView(allOf(new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                return item instanceof EditText;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("withType:" + EditText.class);
            }
        }, isDisplayed())).perform(replaceText("消息发送:" + i));
        onView(allOf(withText("发送"), isDisplayed())).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);
        Espresso.pressBack();
    }

    private void register(IdlingResource idlingResource) {
        idlingResourceList.add(idlingResource);
        Espresso.registerIdlingResources(idlingResource);
    }

    @After
    public void setDown() throws InterruptedException {
        if (idlingResourceList.isEmpty()) {
            return;
        }
        Espresso.unregisterIdlingResources(idlingResourceList.toArray(new IdlingResource[idlingResourceList.size()]));
        Thread.sleep(10000);
    }
}
