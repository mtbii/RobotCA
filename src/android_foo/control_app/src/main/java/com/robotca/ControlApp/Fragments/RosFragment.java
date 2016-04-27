package com.robotca.ControlApp.Fragments;


import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

/**
 * Fragment for a ROS subscriber or publisher.
 *
 * Created by Michael Brunson on 11/8/15.
 */
public abstract class RosFragment extends SimpleFragment {

    /** NodeMainExecutor for launching new nodes */
    protected NodeMainExecutor nodeMainExecutor;
    /** NodeConfiguration for the nodes */
    protected NodeConfiguration nodeConfiguration;

    // Whether this RosFragment's NodeMainExecutor has been started
    private boolean initialized;

    @Override
    public void onDestroyView() {
        shutdown();

        super.onDestroyView();
    }

    /**
     * Called when the Fragment is shutdown.
     */
    abstract void shutdown();

    /**
     * Called when the Fragment is initialized.
     * @param mainExecutor The NodeMainExecutor with which to register subscribers/publishers
     * @param nodeConfiguration The NodeConfiguration
     */
    public void initialize(NodeMainExecutor mainExecutor, NodeConfiguration nodeConfiguration) {
        this.nodeMainExecutor = mainExecutor;
        this.nodeConfiguration = nodeConfiguration;
        setInitialized(true);
    }

    /**
     * @return Whether this Fragment has been initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Sets the initialized value of this Fragment without calling intitialize().
     * @param initialized The new initialized value
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
