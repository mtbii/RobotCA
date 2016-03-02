package com.robotca.ControlApp.Fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robotca.ControlApp.Core.ControlMode;
import com.robotca.ControlApp.R;
import com.robotca.ControlApp.Views.JoystickView;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

/**
 * Fragment containing the JoystickView.
 * Created by Michael Brunson on 11/7/15.
 */
public class JoystickFragment extends RosFragment {
    private JoystickView virtualJoystick;
    private View view;
    private ControlMode controlMode = ControlMode.Joystick;
    private boolean isSetup;

    /**
     * Default Constructor.
     */
    public JoystickFragment() {
    }

    /**
     * Create this Fragments View.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the view if needed
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_joystick_view, container, false);

            // Grab the JoystickView and set its topic
            virtualJoystick = (JoystickView) view.findViewById(R.id.joystick_view);
            virtualJoystick.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("edittext_joystick_topic", getString(R.string.joy_topic)));

            // Start the Joystick's subscribers
            if (!isSetup && isInitialized()) {
                isSetup = true;
                nodeMainExecutor.execute(virtualJoystick, nodeConfiguration.setNodeName("android/virtual_joystick"));
            }
        }

        return view;
    }

    /**
     * Returns the JoystickView
     * @return The JoystickView
     */
    public JoystickView getJoystickView() {
        return virtualJoystick;
    }

    /**
     * Shutowns the JoystickView
     */
    @Override
    void shutdown() {
        if (isSetup && isInitialized()) {
            nodeMainExecutor.shutdownNodeMain(virtualJoystick);
        }

        isSetup = false;
        setInitialized(false);
    }

    /**
     * Get the currently active ControlMode.
     * @return The current ControlMode
     */
    public ControlMode getControlMode() {
        return controlMode;
    }

    /**
     * Set the ControlMode for controlling the Joystick.
     * @param controlMode The new ControlMode
     */
    public void setControlMode(ControlMode controlMode) {
        this.controlMode = controlMode;
        this.invalidate();
    }

    /**
     * Tests whether the Joystick supports accelerometer control.
     * @return True if the Joystick supports accelerometer control, false otherwise
     */
    public boolean hasAccelerometer() {
        return virtualJoystick.hasAccelerometer();
    }

    /**
     * Invalidate the Fragment, updating the visibility of the Joystick based on the ControlMode
     * and initializing the Joystick subscribers if not already done.
     */
    public void invalidate() {
        switch (controlMode) {
            case Joystick:
                show();
                break;

            case Motion:
                show();
                break;

            case Waypoint:
                hide();
                break;

            case RandomWalk:
                hide();
                break;
        }

        if (isInitialized()) {
            if(!isSetup) {
                isSetup = true;
                nodeMainExecutor.execute(virtualJoystick, nodeConfiguration.setNodeName("android/virtual_joystick"));
            }else{
                virtualJoystick.setControlMode(controlMode);
                virtualJoystick.controlSchemeChanged();
            }
        }
    }

    public void stop() {
        virtualJoystick.stop();
    }
}
