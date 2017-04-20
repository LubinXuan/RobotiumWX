package me.robin.espressomodule.actions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.util.Log;
import me.robin.espressomodule.Action;
import me.robin.espressomodule.ContactBroadcastReceiver;
import me.robin.espressomodule.Provider;
import me.robin.espressomodule.WxTestEspresso;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        final String filter = ContactModifyBroadcastReceiver.class.getName() + UUID.randomUUID().toString().replace("-", "");
        Context applicationCtx = InstrumentationRegistry.getTargetContext();
        try {
            IntentFilter intentFilter = new IntentFilter(filter);
            applicationCtx.registerReceiver(receiver, intentFilter);
            JSONArray numbers = taskDefine.getJSONArray("numbers");
            Intent intent = new Intent(ContactBroadcastReceiver.class.getName());
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra("numbers", numbers.toString());
            intent.putExtra("action", filter);
            provider.getInstrumentation().getContext().sendBroadcast(intent);
            receiver.waitFinish();
        } catch (Exception e) {
            Log.e(WxTestEspresso.TAG, "号码列表读取失败", e);
        } finally {
            applicationCtx.unregisterReceiver(receiver);
        }
    }

    public class ContactModifyBroadcastReceiver extends BroadcastReceiver {

        private CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void onReceive(Context context, Intent intent) {
            latch.countDown();
        }

        public void waitFinish() throws InterruptedException {
            latch.await(20, TimeUnit.SECONDS);
        }
    }
}
