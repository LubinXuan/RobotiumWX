package me.robin.espressomodule.actions;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.robin.espressomodule.Action;
import me.robin.espressomodule.Promise;
import me.robin.espressomodule.Provider;
import me.robin.espressomodule.Utils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
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
        Log.i(provider.getLogTag(), "THREAD:" + Thread.currentThread());
        JSONArray numbers = taskDefine.getJSONArray("numbers");
        final String message = taskDefine.getString("message");
        long loopInterval = taskDefine.getIntValue("interval");
        loopInterval = loopInterval > 0 ? loopInterval : 1000;
        final AtomicBoolean reenter = new AtomicBoolean(true);
        for (int i = 0; i < numbers.size(); i++) {

            String searchText = numbers.getString(i);

            if (reenter.get()) {
                onView(allOf(withContentDescription("更多功能按钮"), isDisplayed())).perform(click());
                onView(allOf(withText("添加朋友"), isDisplayed())).perform(click());
                onView(allOf(withText("微信号/QQ号/手机号"), isDisplayed())).perform(click());
            }
            onView(allOf(withHint("搜索"), isDisplayed())).perform(replaceText(searchText));


            onView(allOf(withText("搜索:" + searchText), isDisplayed())).perform(click());
            Utils.waitViewClose(onView(withText("正在查找联系人...")));

            new Promise() {
                @Override
                public void service() {
                    ViewInteraction send = onView(allOf(withText("发消息"), isDisplayed()));
                    send.check(matches(isDisplayed())).perform(click());
                }
            }.withSuccess(new Promise() {
                @Override
                public void service() {
                    onView(allOf(new TypeSafeMatcher<View>() {
                        @Override
                        protected boolean matchesSafely(View item) {
                            return item instanceof EditText;
                        }

                        @Override
                        public void describeTo(Description description) {
                            description.appendText("withType:" + EditText.class);
                        }
                    }, isDisplayed())).perform(replaceText(message));
                    onView(allOf(withText("发送"), isDisplayed())).perform(click());
                    Espresso.pressBack();
                    reenter.set(true);
                }
            }).withFailure(new Promise() {
                @Override
                public void service() {
                    ViewInteraction add = onView(allOf(withText("添加到通讯录"), isDisplayed()));
                    add.check(matches(isDisplayed())).perform(click());
                }
            }.withSuccess(new Promise() {
                @Override
                public void service() {
                    onView(allOf(withText("发送"), isDisplayed())).perform(click());
                    Espresso.pressBack();
                    try {
                        onView(allOf(withText("添加到通讯录"), isDisplayed())).check(matches(isDisplayed()));
                    } catch (Exception e) {
                        Espresso.pressBack();
                    }
                    reenter.set(false);
                }
            }).withFailure(new Promise() {
                @Override
                public void service() {
                    onView(allOf(withText("该用户不存在"), isDisplayed())).check(matches(isDisplayed()));
                    reenter.set(false);
                }
            })).run();

            Utils.sleep(loopInterval);
        }

    }
}
