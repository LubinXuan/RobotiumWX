package me.robin.espressomodule.actions;

import com.alibaba.fastjson.JSONObject;
import me.robin.espressomodule.Action;
import me.robin.espressomodule.Provider;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by xuanlubin on 2017/4/20.
 */
public class PostMomentsAction implements Action<Boolean> {

    @Override
    public boolean isUiRequired() {
        return true;
    }

    @Override
    public Boolean process(JSONObject taskDefine, Provider provider) {
        onView(allOf(withText("发现"), isDisplayed())).perform(click());
        onView(allOf(withText("朋友圈"), isDisplayed())).perform(click());
        onView(allOf(withContentDescription("更多功能按钮"), isDisplayed())).perform(longClick());
        onView(allOf(withHint("这一刻的想法..."), isDisplayed()))
                .perform(replaceText("中文测试 \r\npost by espresso!"));
        onView(allOf(withText("发送"), isDisplayed())).perform(click());
        return true;
    }
}
