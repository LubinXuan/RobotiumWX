package me.robin.espressomodule;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import me.robin.espressomodule.actions.AddMobileAndUpdateWxRelationAction;
import me.robin.espressomodule.actions.PostMomentsAction;
import me.robin.espressomodule.actions.SendMessageAction;
import me.robin.espressomodule.client.CusHeartBeatHandler;
import me.robin.espressomodule.client.NettyClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuanlubin on 2017/4/17.
 */
@RunWith(AndroidJUnit4.class)
public class WxTestEspresso {
    public static final String TAG = "Espresso";
    private static Class<Activity> launchActivityClass;
    // 对应re-sign.jar生成出来的信息框里的两个值

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

    private NettyClient nettyClient;

    private CountDownLatch latch = new CountDownLatch(1);

    private String clientId;

    @Before
    public void setUp() throws InterruptedException {
        Log.i(TAG, "测试任务启动");
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        this.provider = new Provider(TAG, InstrumentationRegistry.getInstrumentation(), device, mActivityRule);
        this.actionMap.put("sendMessage", new SendMessageAction());
        this.actionMap.put("postMoments", new PostMomentsAction());
        this.actionMap.put("addMobileContact", new AddMobileAndUpdateWxRelationAction());
        this.clientId = UUID.randomUUID().toString().replace("-", "");

        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new IdleStateHandler(0, 0, 10, TimeUnit.SECONDS));
                pipeline.addLast(new LengthFieldBasedFrameDecoder(512*1024, 0, 4, -4, 0));
                pipeline.addLast("handler", new CusHeartBeatHandler() {
                    @Override
                    protected void handleData(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws IOException {
                        byte[] data = new byte[byteBuf.readableBytes() - 5];
                        byteBuf.skipBytes(5);
                        byteBuf.readBytes(data);
                        String content = new String(data);
                        if (handleTaskData(channelHandlerContext.channel(), content)) {
                            nettyClient.close();
                        }
                    }

                    @Override
                    protected void handleAllIdle(ChannelHandlerContext ctx) {
                        super.handleAllIdle(ctx);
                        sendPingMsg(ctx);
                    }

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        nettyClient.sendRequest(ctx.channel(), "register:" + clientId);
                        showToast("连接服务器成功");
                        super.channelActive(ctx);
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        showToast("网络连接异常:" + cause.getLocalizedMessage());
                        super.exceptionCaught(ctx, cause);
                    }
                });
            }
        };

        this.nettyClient = new NettyClient(channelInitializer, new NettyClient.NoticeListener() {
            @Override
            public void notice(String message) {
                showToast(message);
            }
        });
    }

    @Test
    public void testMain() throws Exception {
        this.nettyClient.listen("192.168.5.2", 18080);
        this.latch.await();
    }

    private void showToast(final String message) {
        try {
            final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            mActivityRule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private boolean handleTaskData(Channel channel, String content) {
        if (null == content || content.trim().isEmpty()) {
            showToast("没有获取到任务");
            return false;
        }
        if ("stop".equals(content)) {
            latch.countDown();
            return true;
        } else {
            try {
                JSONObject object = JSON.parseObject(content);
                String actionType = object.getString("action");
                Action action = this.actionMap.get(actionType);
                if (null != action) {
                    Object ret = null;
                    try {
                        ret = action.process(object, provider);
                    } catch (Exception e) {

                    } finally {
                        reportActionResult(channel, object, ret);
                    }
                }
            } catch (JSONException ignore) {
                showToast("未知指令:" + content);
            }
        }
        return false;
    }

    private void reportActionResult(Channel channel, JSONObject action, final Object ret) {
        try {
            JSONObject report = new JSONObject();
            report.put("_id", action.getString("_id"));
            report.put("clientId", this.clientId);
            report.put("result", ret);
            this.nettyClient.sendRequest(channel, report.toJSONString());
            showToast("任务处理:" + JSON.toJSONString(ret));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @After
    public void setDown() throws InterruptedException {
        Thread.sleep(10000);
    }
}
