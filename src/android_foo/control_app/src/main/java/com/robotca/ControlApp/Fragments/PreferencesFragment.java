package com.robotca.ControlApp.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.R;

/**
 * Created by Michael Brunson on 11/7/15.
 */
public class PreferencesFragment extends PreferenceFragment {

    public PreferencesFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
            return;

        addPreferencesFromResource(R.xml.prefs);
    }

    @Override
    public void onPause() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ControlApp.ROBOT_INFO.setJoystickTopic(prefs.getString("edittext_joystick_topic", getString(R.string.joy_topic)));
        ControlApp.ROBOT_INFO.setLaserTopic(prefs.getString("edittext_laser_scan_topic", getString(R.string.laser_scan_topic)));
        ControlApp.ROBOT_INFO.setCameraTopic(prefs.getString("edittext_camera_topic", getString(R.string.camera_topic)));

        super.onStop();
    }
}
