package com.robotca.ControlApp.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
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
    public PreferencesFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
            return;

        // Add the preferences
        addPreferencesFromResource(R.xml.prefs);

        // Make the enable warning system checkbox enable/disable the warning system preference menu
        final Preference warningSystemSettings = findPreference(getString(R.string.prefs_warning_system_key));
        findPreference(getString(R.string.prefs_warning_checkbox_key)).setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {

                        if (newValue instanceof Boolean)
                            warningSystemSettings.setEnabled((boolean) newValue);

                        return true;
                    }
                }
        );
    }

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

        if (getActivity() instanceof ControlApp)
            ((ControlApp)getActivity()).onPreferencesChanged(prefs);
        else
            Log.w(TAG, "Could not notify ControlApp!");

        super.onStop();
    }
}
