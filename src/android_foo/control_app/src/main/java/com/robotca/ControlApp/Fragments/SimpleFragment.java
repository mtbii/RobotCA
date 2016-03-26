package com.robotca.ControlApp.Fragments;

import android.app.Fragment;

import com.robotca.ControlApp.ControlApp;

/**
 * Fragment containing basic show() and hide() functionality.
 *
 * Created by Nathaniel Stone on 3/22/16.
 */
public class SimpleFragment extends Fragment {

    /**
     * Shows the Fragment, making it visible.
     */
    public void show(){
        getFragmentManager()
                .beginTransaction()
                .show(this)
                .commit();
    }

    /**
     * Hides the Fragment, making it invisible.
     */
    public void hide(){
        getFragmentManager()
                .beginTransaction()
                .hide(this)
                .commit();
    }

    /**
     * Convenience method to get the current activity as a ControlApp.
     * @return The current activity casted to a ControlApp if it is one and null otherwise
     */
    public ControlApp getControlApp() {
        if (getActivity() instanceof ControlApp)
            return (ControlApp) getActivity();
        else
            return null;
    }
}
