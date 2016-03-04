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
            testString.add(new RobotInfo(null, name, null, null, null, null));
        }

        RobotInfo.resolveRobotCount(testString);

        assertEquals(RobotInfo.getRobotCount(), 8);
    }
}