package com.robotca.ControlApp.Fragments;


import android.app.Fragment;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

/**
 * Created by Michael Brunson on 11/8/15.
 */
public abstract class RosFragment extends Fragment {
    protected NodeMainExecutor nodeMainExecutor;
    protected NodeConfiguration nodeConfiguration;

    @Override
    public void onDestroyView() {
        shutdown();

        super.onDestroyView();
    }

    abstract void shutdown();

    public void initialize(NodeMainExecutor mainExecutor, NodeConfiguration nodeConfiguration) {
        this.nodeMainExecutor = mainExecutor;
        this.nodeConfiguration = nodeConfiguration;
    }
}
