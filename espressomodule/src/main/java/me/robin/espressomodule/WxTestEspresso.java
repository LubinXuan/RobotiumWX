package me.robin.espressomodule;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.robin.espressomodule.actions.AddMobileAndUpdateWxRelationAction;
import me.robin.espressomodule.actions.PostMomentsAction;
import me.robin.espressomodule.actions.SendMessageAction;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuanlubin on 2017/4/17.
 */
@RunWith(AndroidJUnit4.class)
public class WxTestEspresso {
    public static final String TAG = "Espresso";
    private static Class<Activity> launchActivityClass;
    // 对应re-sign.jar生成出来的信息框里的两个值
    private List<IdlingResource> idlingResourceList = new ArrayList<>(32);

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

    private Provider provider;

    private Map<String, Action> actionMap = new HashMap<>();

    private OkHttpClient client;

    @Before
    public void setUp() {
        Log.i(TAG, "测试任务启动");
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        this.provider = new Provider(TAG, InstrumentationRegistry.getInstrumentation(), device, mActivityRule);
        this.actionMap.put("sendMessage", new SendMessageAction());
        this.actionMap.put("postMoments", new PostMomentsAction());
        this.actionMap.put("addMobileContact", new AddMobileAndUpdateWxRelationAction());
        this.client = new OkHttpClient();
    }

    @Test
    public void testMain() throws Exception {
        while (true) {
            Request request = new Request.Builder().url("http://10.2.2.92:2223/tasks").get().build();
            String content;
            try {
                Response response = client.newCall(request).execute();
                content = response.body().string();
            } catch (IOException e) {
                Utils.sleep(2000);
                content = null;
            }
            if (null == content || content.trim().isEmpty()) {
                try {
                    final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
                    mActivityRule.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "没有获取到任务", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                continue;
            }
            JSONObject object = JSON.parseObject(content);
            String actionType = object.getString("action");
            if ("stop".equals(actionType)) {
                break;
            } else {
                Action action = this.actionMap.get(actionType);
                if (null != action) {
                    try {
                        Object ret = action.process(object, provider);
                        reportActionResult(object, ret);
                    } catch (Exception e) {

                    } finally {

                    }
                }
            }
        }
    }

    private void reportActionResult(JSONObject action, final Object ret) {
        try {
            final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            mActivityRule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "任务处理:" + JSON.toJSONString(ret), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
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
