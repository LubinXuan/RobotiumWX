package me.robin.espressomodule.actions;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import me.robin.espressomodule.Action;
import me.robin.espressomodule.Provider;
import me.robin.espressomodule.idle.UserSearchIdlingResource;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONObject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by xuanlubin on 2017/4/20.
 */
public class SendMessageAction implements Action {

    @Override
    public boolean isUiRequired() {
        return true;
    }

    @Override
    public void process(JSONObject taskDefine, Provider provider) throws Exception {
        int messages = 10;
        IdlingResource idlingResource = new UserSearchIdlingResource(provider.getRule());
        for (int i = 0; i < messages; i++) {
            Log.i(provider.getLogTag(), "THREAD:" + Thread.currentThread());
            String searchText = "420027600";
            onView(allOf(withContentDescription("更多功能按钮"), isDisplayed())).perform(click());
            onView(allOf(withText("添加朋友"), isDisplayed())).perform(click());
            onView(allOf(withText("微信号/QQ号/手机号"), isDisplayed())).perform(click());
            onView(allOf(withHint("搜索"), isDisplayed())).perform(replaceText(searchText));


            onView(allOf(withText("搜索:" + searchText), isDisplayed())).perform(click());
            Espresso.registerIdlingResources(idlingResource);
            _sendMessage(idlingResource, provider, i);
            Thread.sleep(1000);
        }
    }

    private void _sendMessage(IdlingResource idlingResource, Provider provider, int i) throws UiObjectNotFoundException {
        //onView(allOf(withText("发消息"), isDisplayed())).perform(click());

        UiObject openChat = provider.getUiDevice().findObject(new UiSelector().text("发消息"));

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
}
