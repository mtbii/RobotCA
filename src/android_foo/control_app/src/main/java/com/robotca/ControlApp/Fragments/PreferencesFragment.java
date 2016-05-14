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

        ControlApp.ROBOT_INFO.load(prefs);

        // Let the ControlApp know that the Preferences have been changed so it can save them
        if (getActivity() instanceof ControlApp)
            ((ControlApp)getActivity()).onPreferencesChanged(prefs);
        else
            Log.w(TAG, "Could not notify ControlApp!");

        super.onStop();
    }
}
