package me.robin.espressomodule.actions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.ViewInteraction;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.robin.espressomodule.*;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsAnything.anything;

/**
 * Created by xuanlubin on 2017/4/20.
 */
public class AddMobileAndUpdateWxRelationAction implements Action {

    @Override
    public boolean isUiRequired() {
        return true;
    }

    @Override
    public void process(JSONObject taskDefine, final Provider provider) throws Exception {
        final ContactModifyBroadcastReceiver receiver = new ContactModifyBroadcastReceiver();
        final ContactBroadcastReceiver contactBroadcastReceiver = new ContactBroadcastReceiver();
        final String filter = ContactModifyBroadcastReceiver.class.getName() + UUID.randomUUID().toString().replace("-", "");
        Context applicationCtx = InstrumentationRegistry.getTargetContext();
        try {
            applicationCtx.registerReceiver(contactBroadcastReceiver,new IntentFilter(ContactBroadcastReceiver.class.getName()));
            IntentFilter intentFilter = new IntentFilter(filter);
            applicationCtx.registerReceiver(receiver, intentFilter);
            JSONArray numbers = taskDefine.getJSONArray("numbers");
            Intent intent = new Intent(ContactBroadcastReceiver.class.getName());
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra("numbers", numbers.toJSONString());
            intent.putExtra("action", filter);
            provider.getInstrumentation().getContext().sendBroadcast(intent);
            receiver.waitFinish();
            startReadMobileWx(provider);
        } catch (Exception e) {
            Log.e(WxTestEspresso.TAG, "号码列表读取失败", e);
        } finally {
            applicationCtx.unregisterReceiver(contactBroadcastReceiver);
            applicationCtx.unregisterReceiver(receiver);
        }
    }

    private void startReadMobileWx(final Provider provider) {
        onView(allOf(withContentDescription("更多功能按钮"), isDisplayed())).perform(click());
        onView(allOf(withText("添加朋友"), isDisplayed())).perform(click());
        onView(allOf(withText("手机联系人"), isDisplayed())).perform(click());


        try {
            onView(allOf(withText("添加手机联系人"), isDisplayed())).perform(click());
            readListView(provider);
        } catch (Exception e) {
            Log.e(WxTestEspresso.TAG, "error onclick 添加手机联系人", e);
        }
    }

    private void readListView(final Provider provider) {

        Utils.waitViewClose(onView(withText("正在获取朋友信息")));

        final AtomicReference<ListView> listViewAtomicReference = new AtomicReference<>();
        ViewInteraction interaction = onView(allOf(new TypeSafeMatcher<View>(ListView.class) {
            @Override
            protected boolean matchesSafely(View item) {
                listViewAtomicReference.set((ListView) item);
                Log.i(WxTestEspresso.TAG, "view 类型:" + item.getClass());
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("withType:" + ListView.class);
            }
        }, isDisplayed())).check(matches(isDisplayed()));


        ListIdlingResource idlingResource = new ListIdlingResource(listViewAtomicReference.get());

        Espresso.registerIdlingResources(idlingResource);
        try {
            ListAdapter listAdapter = listViewAtomicReference.get().getAdapter();
            Log.i(WxTestEspresso.TAG, "ListAdapterType:" + listAdapter.getClass());
            int count = listAdapter.getCount();
            Field[] fields = listAdapter.getClass().getDeclaredFields();
            for (Field field : fields) {
                Log.i(WxTestEspresso.TAG, "字段:" + field.getGenericType());
            }

            List<String> dataArray = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                LinearLayout layout = (LinearLayout) listAdapter.getView(i, null, listViewAtomicReference.get());
                Object item = listAdapter.getItem(i);
                LinearLayout clickAble = (LinearLayout) layout.getChildAt(layout.getChildCount() - 1);
                layout = (LinearLayout) clickAble.getChildAt(1);
                CharSequence mobile = ((TextView) layout.getChildAt(0)).getText();
                CharSequence wxName = ((TextView) layout.getChildAt(1)).getText();
                dataArray.add(mobile + ":" + wxName);
                Log.i(WxTestEspresso.TAG, mobile + ":" + wxName + "\r\n" + JSON.toJSONString(item));
            }


            for (int i = 0; i < count; i++) {
                String data = dataArray.get(i);
                Log.i(WxTestEspresso.TAG, "data:" + data);
                onData(anything()).atPosition(i).perform(click());
                Utils.sleep(2000);
                Espresso.pressBack();
            }

        } finally {
            Espresso.unregisterIdlingResources(idlingResource);
        }
    }

    class ListIdlingResource implements IdlingResource {

        IdlingResource.ResourceCallback callback;

        ListView listView;

        public ListIdlingResource(ListView listView) {
            this.listView = listView;
        }

        @Override
        public String getName() {
            return UUID.randomUUID().toString();
        }

        @Override
        public boolean isIdleNow() {
            return listView.getAdapter() != null;
        }

        @Override
        public void registerIdleTransitionCallback(IdlingResource.ResourceCallback callback) {
            this.callback = callback;
        }
    }

    public class ContactModifyBroadcastReceiver extends BroadcastReceiver {

        private CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void onReceive(Context context, Intent intent) {
            latch.countDown();
        }

        public void waitFinish() throws InterruptedException {
            latch.await(5, TimeUnit.SECONDS);
        }
    }
}
