package com.robotca.ControlApp.Core.Plans;

import com.robotca.ControlApp.Core.RobotController;

import geometry_msgs.Twist;
import sensor_msgs.LaserScan;
import sensor_msgs.NavSatFix;

/**
 * Created by Michael Brunson on 2/13/16.
 */
public interface IRobotPlan {
    void run(RobotController controller) throws Exception;
}
