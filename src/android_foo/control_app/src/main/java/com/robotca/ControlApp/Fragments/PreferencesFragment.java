package com.robotca.ControlApp.Fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

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
}
