package com.robotca.ControlApp.Core;

import com.robotca.ControlApp.ControlApp;

import org.ros.message.MessageListener;

import sensor_msgs.LaserScan;

/**
 * Warning system for alerting ControlApp when a collision is imminent.
 * Created by Nathaniel on 3/31/16 (Adapted from WarningSystemPlan created by lightyz on 3/24/16).
 */
public class WarningSystem implements MessageListener<LaserScan> {

    private final ControlApp controlApp;
    private final float minRange;

    private static final float ANGLE_DELTA = (float) Math.toRadians(30.0);

    // Log tag String
    @SuppressWarnings("unused")
    private static final String TAG = "WarningSystem";

    /**
     * Creates a WarningSystem plan for the specified ControlApp.
     * @param controlApp The ControlApp
     * @param minRange The minimum range deemed dangerous
     */
    public WarningSystem(ControlApp controlApp, float minRange) {
        this.controlApp = controlApp;
        this.minRange = Math.max(minRange, 0.2f);
    }

    @Override
    public void onNewMessage(LaserScan laserScan) {
        float[] ranges = laserScan.getRanges();
        float shortestDistance = ranges[ranges.length / 2];

//        Log.d(TAG, "Original shortest distance: " + shortestDistance);

        float angle = laserScan.getAngleMin();

        // Correct for the Robot's turn rate
        angle += (float) RobotController.getTurnRate() * 0.5f;

        float angleIncrement = laserScan.getAngleIncrement();

        for (int i = 0; i < laserScan.getRanges().length; i++) {
            if (ranges[i] < shortestDistance && angle > -ANGLE_DELTA && angle < ANGLE_DELTA) {
                shortestDistance = ranges[i];
            }

            angle += angleIncrement;
        }

        // Warn the ControlApp if necessary
        if (RobotController.getSpeed() > -0.1 &&
                shortestDistance < minRange * Math.max(0.4, RobotController.getSpeed())) {
            controlApp.collisionWarning();
        }
    }
}
