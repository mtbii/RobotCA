package com.robotca.ControlApp.Core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Simple Unit Test for RobotInfo.
 * Created by Nathaniel Stone on 3/3/16.
 */
public class RobotInfoTest {

    @Test
    public void testResolveRobotCount() throws Exception {

        List<RobotInfo> testString = new ArrayList<>();

        for (String name: new String[] {"Robot2", "Robot3", "Robot-2", "Robot3d", "R0b0t11", "Robot7"})
        {
            testString.add(new RobotInfo(null, name, null, null, null, null, null, null, null));
        }

        RobotInfo.resolveRobotCount(testString);

        assertEquals(RobotInfo.getRobotCount(), 8);
    }

    @Test
    public void testRobotInfo() throws Exception{

        RobotInfo robot1 = new RobotInfo();
        assertEquals(robot1.getName(), "Robot" + (RobotInfo.getRobotCount() - 1));
        assertEquals(robot1.getMasterUri(), "http://localhost:11311");
        assertEquals(robot1.getJoystickTopic(), "/joy_teleop/cmd_vel");
        assertEquals(robot1.getCameraTopic(), "/image_raw/compressed");
        assertEquals(robot1.getLaserTopic(), "/scan");
        RobotInfo robot2 = new RobotInfo(null, "robot2", "http://localhost:11311", null, null, null, null, null, null);
        assertEquals(robot2.getMasterUri(), robot1.getMasterUri());
    }
}
