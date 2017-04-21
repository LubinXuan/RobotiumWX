package android.support.test.espresso.action;

import android.support.test.espresso.ViewAction;

import static android.support.test.espresso.action.ViewActions.actionWithAssertions;

/**
 * Created by xuanlubin on 2017/4/21.
 */
public class Swipes {
    public static ViewAction up(Swipe swipe) {
        return actionWithAssertions(new GeneralSwipeAction(swipe,
                GeneralLocation.translate(GeneralLocation.BOTTOM_CENTER, 0, -0.083f),
                GeneralLocation.TOP_CENTER, Press.FINGER));
    }
}
