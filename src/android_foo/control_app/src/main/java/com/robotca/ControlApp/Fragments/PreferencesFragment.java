package com.robotca.ControlApp.Fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

//    @Override
//    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
//    {
//        View view = super.onCreateView(layoutInflater, viewGroup, bundle);
//
//        if (view != null) {
//            ListView list = (ListView) view.findViewById(android.R.id.list);
//            list.setDivider(null);
//        }
//
//        return view;
//    }


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
