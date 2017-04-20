package me.robin.espressomodule;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;
import me.robin.espressomodule.actions.AddMobileAndUpdateWxRelationAction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

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

    @Before
    public void setUp() {
        Log.i(TAG, "测试任务启动");
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        this.provider = new Provider(TAG, InstrumentationRegistry.getInstrumentation(), device, mActivityRule);
    }

    @Test
    public void testMain() throws Exception {
        /*while (true) {

        }*/
        JSONObject jsonObject = new JSONObject();
        JSONArray numbers = new JSONArray();
        for (int i = 0; i < 10; i++) {
            numbers.put("12345678" + i);
        }
        jsonObject.put("numbers", numbers);
        new AddMobileAndUpdateWxRelationAction().process(jsonObject, provider);
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
