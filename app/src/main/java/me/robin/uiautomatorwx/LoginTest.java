package me.robin.uiautomatorwx;


import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.robotium.solo.SoloExt;
import me.robin.uiautomatorwx.util.PositionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("rawtypes")
public class LoginTest extends ActivityInstrumentationTestCase2 {

    public static final String TAG = "Robotium";

    public SoloExt solo;
    public Activity activity;
    private static Class<?> launchActivityClass;
    // 对应re-sign.jar生成出来的信息框里的两个值
    private static String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "com.tencent.mm.ui.LauncherUI";
    private static String TARGET_PACKAGE_ID = "com.tencent.mm";


    static {
        try {
            launchActivityClass = Class.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @SuppressWarnings("unchecked")
    public LoginTest() {
        super(TARGET_PACKAGE_ID, launchActivityClass);
    }


    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        this.activity = this.getActivity();
        if (null == this.solo) {
            this.solo = new SoloExt(getInstrumentation(), getActivity());
        }
    }

    @Test
    public void testLogin() throws Exception {
        this.solo.sleep(10000);
        RelativeLayout moreClick = this.solo.getViewByDesc("更多功能按钮", RelativeLayout.class);
        if (null != moreClick) {
            Log.i(TAG, "search view:" + moreClick.getContentDescription());
            this.solo.clickLongOnView(moreClick, 100);
            PositionUtils.showPosition(moreClick);
            ListView moreItems = this.solo.getView(ListView.class, 0);
            Log.i(TAG, "moreItems list view " + moreItems.getChildCount());
            this.solo.clickOnText("添加朋友");
            this.solo.clickOnText("微信号/QQ号/手机号");
            EditText editText = this.solo.getEditText("搜索");
            Log.i(TAG, "search activity:" + this.solo.getCurrentActivity().getClass());
            this.solo.enterText(editText, "18969049096中文。");
            this.solo.sleep(5000);
            View content = this.solo.getView("content");
            LinearLayout searchLayout = this.solo.getView(LinearLayout.class, content);
            this.solo.clickOnView(searchLayout, true);
        }
        this.solo.sleep(10000);
    }


    @Test
    public void testMoments() {
        this.solo.sleep(10000);
        this.solo.clickOnText("发现");
        this.solo.clickOnText("朋友圈");
        this.solo.sleep(2000);
        RelativeLayout moreClick = this.solo.getViewByDesc("更多功能按钮", RelativeLayout.class);
        if (null != moreClick) {
            this.solo.clickLongOnView(moreClick, 2000);
            EditText editText = this.solo.getEditText("这一刻的想法...");
            this.solo.enterText(editText, "中文测试 \r\npost by robotium!");
            TextView textView = this.solo.getText("发送");
            this.solo.clickOnView(textView);
        }
        this.solo.sleep(10000);
    }


    @Override
    @After
    public void tearDown() throws Exception {
        try {
            this.solo.finishOpenedActivities();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        this.activity.finish();
        super.tearDown();
    }


}
