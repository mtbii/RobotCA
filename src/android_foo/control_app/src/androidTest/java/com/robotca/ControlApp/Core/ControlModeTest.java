package com.robotca.ControlApp.Core;

import android.support.test.rule.ActivityTestRule;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.Plans.RandomWalkPlan;
import com.robotca.ControlApp.Core.Plans.SimpleWaypointPlan;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ControlMode test.
 *
 * Created by Nathaniel on 4/2/16.
 */
public class ControlModeTest {

    static
    {
        ControlApp.ROBOT_INFO = new RobotInfo(null, "HUDTestRobotInfo", "HUDMasterURI", null, null, null, null, null, null, false, false, false, false);
    }

    @Rule
    public ActivityTestRule<ControlApp> controlAppRule = new ActivityTestRule<>(ControlApp.class);

    @Test
    public void testGetRobotPlan() throws Exception {

        // Make sure Joystick has no MotionPlan
        assertEquals(null, ControlMode.getRobotPlan(controlAppRule.getActivity(), ControlMode.Joystick));

        // Test motion plan types
        assertEquals(true,
                ControlMode.getRobotPlan(controlAppRule.getActivity(), ControlMode.RandomWalk)
                        instanceof RandomWalkPlan);

        // Test motion plan types
        assertEquals(true,
                ControlMode.getRobotPlan(controlAppRule.getActivity(), ControlMode.SimpleWaypoint)
                        instanceof SimpleWaypointPlan);
    }
}
