package com.robotca.ControlApp.Core;

import android.preference.PreferenceManager;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.Plans.RandomWalkPlan;
import com.robotca.ControlApp.Core.Plans.RobotPlan;
import com.robotca.ControlApp.Core.Plans.SimpleWaypointPlan;
import com.robotca.ControlApp.Core.Plans.WaypointPlan;

/**
 * Enum for different ways to control the Robot.
 *
 * Created by Michael Brunson on 2/12/16.
 */
public enum ControlMode {
    Joystick, // Joystick control
    Tilt, // Tilt sensor control
    SimpleWaypoint, // SimpleWaypoint control
    Waypoint, // Potential field waypoint control
    RandomWalk; // Random walk

    /**
     * Creates a RobotPlan for the specified ControlMode if one exists.
     * @param controlApp The ControlApp
     * @param controlMode The ControlMode
     * @return A RobotPlan for the ControlMode or null if none exists
     */
    public static RobotPlan getRobotPlan(ControlApp controlApp, ControlMode controlMode) {

        RobotPlan plan;

        switch (controlMode) {

            case SimpleWaypoint: plan = new SimpleWaypointPlan(controlApp); break;
            case Waypoint: plan = new WaypointPlan(controlApp); break;
            case RandomWalk: plan = new RandomWalkPlan(
                    Float.parseFloat(PreferenceManager
                            .getDefaultSharedPreferences(controlApp)
                            .getString("edittext_random_walk_range_proximity", "1")));
                break;
            default: plan = null; break;
        }

        return plan;
    }
}
