package com.robotca.ControlApp.Fragments;


import android.app.Fragment;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

/**
 * Created by Michael Brunson on 11/8/15.
 */
public interface IRosInitializer {

    void initialize(NodeMainExecutor mainExecutor, NodeConfiguration nodeConfiguration);
}
