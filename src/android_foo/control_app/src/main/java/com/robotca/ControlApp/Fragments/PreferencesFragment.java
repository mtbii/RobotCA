package com.robotca.ControlApp.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.R;

/**
 * Fragment containing the Preferences screen.
 *
 * Created by Michael Brunson on 11/7/15.
 */
public class PreferencesFragment extends PreferenceFragment {

    // Log tag String
    private static final String TAG = "PreferencesFragment";

    /**
     * Default Constructor.
     */
    public PreferencesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            return;

        // Add the preferences
        addPreferencesFromResource(R.xml.prefs);
    }

    /**
     * Called when the user has finished editing/viewing the current Preferences.
     * Updates the topic names on the current RobotInfo to be in sync with the Preferences.
     */
    @Override
    public void onPause() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ControlApp.ROBOT_INFO.setJoystickTopic(
                prefs.getString(getString(R.string.prefs_joystick_topic_edittext_key),
                    getString(R.string.joy_topic)));

        ControlApp.ROBOT_INFO.setLaserTopic(
                prefs.getString(getString(R.string.prefs_laserscan_topic_edittext_key),
                        getString(R.string.laser_scan_topic)));

        ControlApp.ROBOT_INFO.setCameraTopic(
                prefs.getString(getString(R.string.prefs_camera_topic_edittext_key),
                        getString(R.string.camera_topic)));

        ControlApp.ROBOT_INFO.setOdometryTopic(prefs.getString(getString(R.string.prefs_odometry_topic_edittext_key),
                getString(R.string.odometry_topic)));

        ControlApp.ROBOT_INFO.setNavSatTopic(prefs.getString(getString(R.string.prefs_navsat_topic_edittext_key),
                getString(R.string.navsat_topic)));

        ControlApp.ROBOT_INFO.setPoseTopic(prefs.getString(getString(R.string.prefs_pose_topic_edittext_key),
                getString(R.string.pose_topic)));

        // Let the ControlApp know that the Preferences have been changed so it can save them
        if (getActivity() instanceof ControlApp)
            ((ControlApp)getActivity()).onPreferencesChanged(prefs);
        else
            Log.w(TAG, "Could not notify ControlApp!");

        super.onStop();
    }
}
