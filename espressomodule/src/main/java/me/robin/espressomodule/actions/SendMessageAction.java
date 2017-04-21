package me.robin.espressomodule.actions;

import android.support.test.espresso.Espresso;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.alibaba.fastjson.JSONObject;
import me.robin.espressomodule.Action;
import me.robin.espressomodule.Provider;
import me.robin.espressomodule.Utils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
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
        int messages = 10;
        for (int i = 0; i < messages; i++) {
            Log.i(provider.getLogTag(), "THREAD:" + Thread.currentThread());
            String searchText = "420027600";
            onView(allOf(withContentDescription("更多功能按钮"), isDisplayed())).perform(click());
            onView(allOf(withText("添加朋友"), isDisplayed())).perform(click());
            onView(allOf(withText("微信号/QQ号/手机号"), isDisplayed())).perform(click());
            onView(allOf(withHint("搜索"), isDisplayed())).perform(replaceText(searchText));


            onView(allOf(withText("搜索:" + searchText), isDisplayed())).perform(click());
            Utils.waitViewClose(onView(withText("正在查找联系人")));
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
            Espresso.pressBack();
        }
    }
}
