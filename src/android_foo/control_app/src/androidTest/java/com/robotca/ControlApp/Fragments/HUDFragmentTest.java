package com.robotca.ControlApp.Fragments;

import android.location.Location;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.RobotInfo;
import com.robotca.ControlApp.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Test class for testing the HUDFragment.
 *
 * Created by Nathaniel Stone on 3/3/16.
 */
@RunWith(AndroidJUnit4.class)
public class HUDFragmentTest {

    static
    {
        ControlApp.ROBOT_INFO = new RobotInfo(null, "HUDTestRobotInfo", "HUDMasterURI", null, null, null, null, null, null, false, false, false, false);
    }

    private static final String TAG = "HUDFragmentTest";

    private static final double TEST_SPEED = 100.0;

    @Rule
    public ActivityTestRule<ControlApp> controlAppRule = new ActivityTestRule<>(ControlApp.class);

    private HUDFragment hudFragment;

    @Before
    public void start()
    {
        hudFragment = (HUDFragment) controlAppRule.getActivity().
                getFragmentManager().findFragmentById(R.id.hud_fragment);
    }

    /**
     * Tests updating UI elements.
     * @throws Exception
     */
    @Test
    public void testUpdateUI() throws Exception {

        Log.d(TAG, "testUpdateUI()");

        // Create a random Location to test the GPS display
        Location loc = new Location("TestProvider");
        loc.setLatitude(360.0 * Math.random() - 180.0);
        loc.setLongitude(360.0 * Math.random() - 180.0);

        String strLongitude = Location.convert(loc.getLongitude(), Location.FORMAT_SECONDS);
        String strLatitude = Location.convert(loc.getLatitude(), Location.FORMAT_SECONDS);

        strLongitude = HUDFragment.getLatLongString(strLongitude, false);
        strLatitude = HUDFragment.getLatLongString(strLatitude, true);

        hudFragment.updateUI(100.0, 360.0);

        // Wait for changes to happen
        Thread.sleep(500L);

        // Test the text
        Log.d(TAG, " testing speed text...");
        onView(withId(R.id.hud_speed)).check(matches(withText(
                String.format(controlAppRule.getActivity().getString(R.string.speed_string), TEST_SPEED))));
    }
}
