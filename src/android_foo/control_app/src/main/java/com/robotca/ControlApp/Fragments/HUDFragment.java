package com.robotca.ControlApp.Fragments;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robotca.ControlApp.Core.RobotGPSSub;
import com.robotca.ControlApp.R;

import org.ros.message.MessageListener;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import nav_msgs.Odometry;

/**
 * Simple fragment showing info about the Robot's current state.
 *
 *
 * @author Nathaniel Stone
 */
public class HUDFragment extends RosFragment implements MessageListener<Odometry>{

    private static final String TAG = "HUDFragment";

    private View view;
    private TextView speedView, turnrateView, locationView;

    private final UpdateUIRunnable UPDATE_UI_RUNNABLE = new UpdateUIRunnable();

    // Node for receiving GPS events
    private RobotGPSSub robotGPSNode;

    private boolean isSetup;

    /**
     * Default Constructor.
     */
    public HUDFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_hud, container, false);

            speedView = (TextView) view.findViewById(R.id.hud_speed);
            turnrateView = (TextView) view.findViewById(R.id.hud_turnrate);
            locationView = (TextView) view.findViewById(R.id.hud_location);

            updateUI(0.0, 0.0);
        }

        // Create the GPS Node
        if (robotGPSNode == null)
            robotGPSNode = new RobotGPSSub();

        return view;
    }

    @Override
    public void initialize(NodeMainExecutor mainExecutor, NodeConfiguration nodeConfiguration)
    {
        super.initialize(mainExecutor, nodeConfiguration);

        if (!isSetup) {
            isSetup = true;

            Log.d(TAG, "initialized: " + nodeMainExecutor + " " + robotGPSNode);

            nodeMainExecutor.execute(robotGPSNode, nodeConfiguration.setNodeName("android/ros_gps"));
        }
    }

    public RobotGPSSub getRobotGPSNode() {
        return robotGPSNode;
    }

    /**
     * Callback for receiving odometry messages.
     * @param message The Odometry message
     */
    @Override
    public void onNewMessage(Odometry message) {
//            Log.d(TAG, "New Message: " + message.getTwist().getTwist().getLinear().getX());

        updateUI(message.getTwist().getTwist().getLinear().getX(),
                message.getTwist().getTwist().getAngular().getZ());
    }

    /**
     * Shuts down the GPS Node
     */
    @Override
    public void shutdown(){

        if (isInitialized()) {
            nodeMainExecutor.shutdownNodeMain(robotGPSNode);
        }
    }

    /*
     *
     */
    private void updateUI(final double speed, final double turnrate)
    {
        if (!isDetached()) {
            UPDATE_UI_RUNNABLE.speed = speed;
            UPDATE_UI_RUNNABLE.turnrate = turnrate;

            view.post(UPDATE_UI_RUNNABLE);
        }
    }

    /*
     * Runnable for refreshing the HUD's UI.
     */
    private class UpdateUIRunnable implements Runnable
    {
        public double speed, turnrate;

        /**
         * Starts executing the active part of the class' code. This method is
         * called when a thread is started that has been created with a class which
         * implements {@code Runnable}.
         */
        @Override
        public void run() {

            if (isDetached())
                return;

            try {
                speed = (int) (speed * 100.0) / 100.0;
                turnrate = (int) (turnrate * 100.0) / 100.0;

                if (speedView != null)
                    speedView.setText(String.format((String) getText(R.string.speed_string), speed));

                if (turnrateView != null)
                    turnrateView.setText(String.format((String) getText(R.string.turnrate_string), turnrate));

                if (locationView != null) {
                    Location loc = robotGPSNode.getLastKnownLocation();

                    if (loc != null) {
                        String strLongitude = Location.convert(loc.getLongitude(), Location.FORMAT_SECONDS);
                        String strLatitude = Location.convert(loc.getLatitude(), Location.FORMAT_SECONDS);

                        locationView.setText(String.format((String) getText(R.string.location_string),
                                strLatitude, strLongitude));
                    }
                }

            } catch (IllegalStateException e) {
                // Ignore
            }
        }
    }
}
