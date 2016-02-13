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
    private boolean initialized;

    @Override
    public void onDestroyView() {
        shutdown();

        super.onDestroyView();
    }

    public void show(){
        getFragmentManager()
                .beginTransaction()
                .show(this)
                .commit();
    }

    public void hide(){
        getFragmentManager()
                .beginTransaction()
                .hide(this)
                .commit();
    }

    abstract void shutdown();

    public void initialize(NodeMainExecutor mainExecutor, NodeConfiguration nodeConfiguration) {
        this.nodeMainExecutor = mainExecutor;
        this.nodeConfiguration = nodeConfiguration;
        setInitialized(true);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
