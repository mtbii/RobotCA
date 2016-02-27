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
 * Created by Michael Brunson on 11/7/15.
 */
public class JoystickFragment extends RosFragment {
    private JoystickView virtualJoystick;
    private View view;
    private ControlMode controlMode = ControlMode.Joystick;
    private boolean isSetup;

    public JoystickFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        if (savedInstanceState != null)
//            return view;

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_joystick_view, container, false);

            virtualJoystick = (JoystickView) view.findViewById(R.id.joystick_view);

            virtualJoystick.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("edittext_joystick_topic", getString(R.string.joy_topic)));

            if (!isSetup && isInitialized()) {
                isSetup = true;
                nodeMainExecutor.execute(virtualJoystick, nodeConfiguration.setNodeName("android/virtual_joystick"));
            }
        }

        return view;
    }

    public JoystickView getJoystickView() {
        return virtualJoystick;
    }

    @Override
    void shutdown() {
        if (isSetup && isInitialized()) {
            nodeMainExecutor.shutdownNodeMain(virtualJoystick);
        }

        isSetup = false;
        setInitialized(false);
    }

    public ControlMode getControlMode() {
        return controlMode;
    }

    public void setControlMode(ControlMode controlMode) {
        this.controlMode = controlMode;
        this.invalidate();
    }

    public boolean hasAccelerometer() {
        return virtualJoystick.hasAccelerometer();
    }

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
