package me.robin.espressomodule.actions;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.alibaba.fastjson.JSONObject;
import me.robin.espressomodule.*;
import me.robin.espressomodule.client.NettyClient;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.concurrent.CountDownLatch;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by Administrator on 2017-05-04.
 */
public class LoginCheckAction implements Action<Boolean> {

    private NettyClient client;

    private CountDownLatch latch = new CountDownLatch(1);

    private Promise checkPromise = new Promise() {
        @Override
        public void service() {
            onView(withText("通讯录")).check(matches(isDisplayed()));
        }
    }.withSuccess(new Promise() {
        @Override
        public void service() {
            Utils.showToast("检测到登录状态");
            latch.countDown();
        }
    }).withFailure(new Promise() {
        @Override
        public void service() {
            client.sendRequest("account");
            Utils.showToast("未登录");
        }
    });

    public LoginCheckAction(NettyClient client) {
        this.client = client;
    }

    @Override
    public boolean isUiRequired() {
        return true;
    }

    @Override
    public Boolean process(JSONObject taskDefine, Provider provider) throws Exception {
        if (null == taskDefine) {
            checkPromise.run();
            latch.await();
        } else {
            onView(withText("更多")).perform(click());
            onView(withText("切换帐号")).perform(click());
            Utils.sleep(5000);
            String account = taskDefine.getString("account");
            String password = taskDefine.getString("password");
            onView(allOf(withHint("你的手机号码"),isDisplayed())).perform(replaceText(account));
            onView(allOf(withHint("填写密码"),isDisplayed())).perform(replaceText(password));
            onView(allOf(withText("登录"), isDisplayed())).perform(click());
            Utils.waitViewClose(onView(withText("正在登录...")));
            checkPromise.run();
        }
        return true;
    }
}
