package com.robotca.ControlApp.Fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robotca.ControlApp.R;
import com.robotca.ControlApp.Views.JoystickView;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

/**
 * Created by Michael Brunson on 11/7/15.
 */
public class JoystickFragment extends RosFragment {
    private JoystickView virtualJoystick;

    public JoystickFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(savedInstanceState != null)
            return virtualJoystick;


        View view = inflater.inflate(R.layout.fragment_joystick_view, null);

        virtualJoystick = (JoystickView) view.findViewById(R.id.joystick_fragment_virtual_joystick);

        virtualJoystick.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("edittext_joystick_topic", getString(R.string.joy_topic)));

        if (nodeConfiguration != null)
            nodeMainExecutor.execute(virtualJoystick, nodeConfiguration.setNodeName("android/virtual_joystick"));

        return view;
    }

    @Override
    void shutdown() {
        nodeMainExecutor.shutdownNodeMain(virtualJoystick);
    }
}
