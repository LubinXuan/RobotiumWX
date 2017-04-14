package me.robin.uiautomatorwx.util;

import android.util.Log;
import android.view.View;
import me.robin.uiautomatorwx.LoginTest;

/**
 * Created by xuanlubin on 2017/4/14.
 */
public class PositionUtils {

    public static void showPosition(View view) {
        int[] xyLocation = new int[2];
        view.getLocationOnScreen(xyLocation);
        final int viewWidth = view.getWidth();
        final int viewHeight = view.getHeight();
        final float x = xyLocation[0] + (viewWidth / 2.0f);
        float y = xyLocation[1] + (viewHeight / 2.0f);
        Log.i(LoginTest.TAG, "position: [" + xyLocation[0] + "," + xyLocation[1] + "]");
        Log.i(LoginTest.TAG, "size: [" + viewWidth + "," + viewHeight + "]");
        Log.i(LoginTest.TAG, "click: [" + x + "," + y + "]");
    }
}
