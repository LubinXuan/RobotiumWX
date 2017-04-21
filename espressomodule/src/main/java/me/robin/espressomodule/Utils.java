package me.robin.espressomodule;

import android.support.test.espresso.ViewInteraction;
import junit.framework.AssertionFailedError;

import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by xuanlubin on 2017/4/21.
 */
public class Utils {

    private static final long checkSleep = 100;

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
}
