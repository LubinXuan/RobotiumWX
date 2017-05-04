package me.robin.espressomodule;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.widget.Toast;
import junit.framework.AssertionFailedError;

import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by xuanlubin on 2017/4/21.
 */
public class Utils {

    public static ActivityTestRule<Activity> mActivityRule;

    private static Toast toast = null;

    private static final long checkSleep = 100;

    public static void setmActivityRule(ActivityTestRule<Activity> mActivityRule) {
        Utils.mActivityRule = mActivityRule;
    }

    public static void sleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void waitViewClose(ViewInteraction viewInteraction) {
        while (true) {
            try {
                viewInteraction.check(doesNotExist());
                break;
            } catch (AssertionFailedError e) {
                sleep(checkSleep);
            }
        }
    }

    public static void waitViewInvisiable(ViewInteraction viewInteraction) {
        while (true) {
            try {
                viewInteraction.check(matches(not(isDisplayed())));
                break;
            } catch (AssertionFailedError e) {
                sleep(checkSleep);
            }
        }
    }


    public static void showToast(final String message) {

        if (null == mActivityRule) {
            return;
        }

        try {
            final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            mActivityRule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (toast == null) {
                        toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                    } else {
                        toast.setText(message);
                    }
                    toast.show();

                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
