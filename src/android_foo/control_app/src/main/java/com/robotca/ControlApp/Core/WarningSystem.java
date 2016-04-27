package com.robotca.ControlApp.Core;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.R;

import org.ros.message.MessageListener;

import sensor_msgs.LaserScan;

/**
 * Warning system for alerting ControlApp when a collision is imminent.
 * Created by Nathaniel on 3/31/16 (Adapted from WarningSystemPlan created by lightyz on 3/24/16).
 */
public class WarningSystem implements MessageListener<LaserScan> {

    private final ControlApp controlApp;

    private final float minRange;
    private boolean enabled;
    private boolean safemode;

    private static final float ANGLE_DELTA = (float) Math.toRadians(40.0);

    /** The minimum distance at which to register laser scan points as dangerous */
    public static final float MIN_DISTANCE = 0.25f;

    // Log tag String
    @SuppressWarnings("unused")
    private static final String TAG = "WarningSystem";

    /**
     * Creates a WarningSystem plan for the specified ControlApp.
     * @param controlApp The ControlApp
     */
    public WarningSystem(ControlApp controlApp) {
        this.controlApp = controlApp;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(controlApp);

        this.enabled = prefs.getBoolean(controlApp.getString(R.string.prefs_warning_system_key), true);
        this.safemode = prefs.getBoolean(controlApp.getString(R.string.prefs_warning_safemode_key), true);

        String val = prefs.getString(controlApp.getString(R.string.prefs_warning_mindist_key),
                controlApp.getString(R.string.default_warning_mindist));
        this.minRange = Math.max(0.2f, Float.parseFloat(val));
    }

    /**
     * Enables/Disables the Warning System.
     * @param enabled Whether to enable or disable the Warning System
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Enables/Disables safe mode.
     * @param enable Whether to enable or disable safe mode
     */
    public void enableSafemode(boolean enable) {
        safemode = enable;
    }

    /**
     * @return Whether safe mode is enable
     */
    public boolean isSafemodeEnabled() {
        return safemode;
    }

    @Override
    public void onNewMessage(LaserScan laserScan) {

        if (!enabled)
            return;

        float[] ranges = laserScan.getRanges();
        float shortestDistance = ranges[ranges.length / 2];

//        Log.d(TAG, "Original shortest distance: " + shortestDistance);

        float angle = laserScan.getAngleMin();

        // Correct for the Robot's turn rate
        angle += (float) RobotController.getTurnRate();

        float angleIncrement = laserScan.getAngleIncrement();

        for (int i = 0; i < laserScan.getRanges().length; i++) {
            if (ranges[i] > MIN_DISTANCE && ranges[i] < shortestDistance
                    && angle > -ANGLE_DELTA && angle < ANGLE_DELTA) {
                shortestDistance = ranges[i];
            }

            angle += angleIncrement;
        }

        // Warn the ControlApp if necessary
        if (RobotController.getSpeed() > -0.1 &&
                shortestDistance < minRange * Math.max(0.5, RobotController.getSpeed())) {

            controlApp.collisionWarning();
        }
    }
}
