package com.robotca.ControlApp.Fragments;

import android.content.Context;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private TextView speedView, turnrateView, locationView, latView, longView;
    private ImageView wifiStrengthView;

    private final UpdateUIRunnable UPDATE_UI_RUNNABLE = new UpdateUIRunnable();
    private final Updater UPDATER = new Updater();

    // Node for receiving GPS events
    private RobotGPSSub robotGPSNode;

    // Used for getting connection strength info
    private WifiManager wifiManager;

    private boolean isSetup;

    private double lastSpeed, lastTurnrate;
    private int lastWifiImage;

    private static int[] wifiIcons;

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
//            locationView = (TextView) view.findViewById(R.id.hud_location);
            latView = (TextView) view.findViewById(R.id.hud_gps_lat);
            longView = (TextView) view.findViewById(R.id.hud_gps_long);

            wifiStrengthView = (ImageView) view.findViewById(R.id.hud_wifi_strength);

            updateUI(0.0, 0.0);
        }

        // Create the GPS Node
        if (robotGPSNode == null)
            robotGPSNode = new RobotGPSSub();

        // Get WifiManager
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        wifiIcons = new int[] {
                R.drawable.wifi_0,
                R.drawable.wifi_1,
                R.drawable.wifi_2,
                R.drawable.wifi_3,
                R.drawable.wifi_4};

        return view;
    }

    @Override
    public void initialize(NodeMainExecutor mainExecutor, NodeConfiguration nodeConfiguration)
    {
        super.initialize(mainExecutor, nodeConfiguration);

        if (!isSetup) {
            isSetup = true;
            nodeMainExecutor.execute(robotGPSNode, nodeConfiguration.setNodeName("android/ros_gps"));
        }

        // Start the Update
        new Thread(UPDATER).start();
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

        UPDATER.kill();
    }

    /*
     *
     */
    void updateUI(final double speed, final double turnrate)
    {
        if (!isDetached()) {
            lastSpeed = speed;
            lastTurnrate = turnrate;

            view.post(UPDATE_UI_RUNNABLE);
        }
    }

    /*
     *
     */
    RobotGPSSub getGPSSub()
    {
        return robotGPSNode;
    }

    static String getLatLongString(String str, boolean lat)
    {
        String r = str.replaceFirst(":", "\u00B0 ").replaceFirst(":", "' ") + "\"";

        if (lat){
            if (r.contains("-")) {
                r = r.replace("-", "") + " S";
            }
            else {
                r += " N";
            }
        }
        else {
            if (r.contains("-")) {
                r = r.replace("-", "") + " W";
            }
            else {
                r += " E";
            }
        }

        return r;
    }

    /*
     * Thread for periodically checking state info.
     */
    private class Updater implements Runnable
    {
        private boolean alive;
        private static final long SLEEP = 1000L;

        /**
         * Run
         */
        @Override
        public void run() {
            alive = true;

            int rssi, temp;

            while (alive) {

//                Log.d(TAG, "RSSI: " + wifiManager.getConnectionInfo().getRssi());

                rssi = wifiManager.getConnectionInfo().getRssi();
                temp = lastWifiImage;
                lastWifiImage = WifiManager.calculateSignalLevel(rssi, 5);

                if (temp != lastWifiImage)
                    view.post(UPDATE_UI_RUNNABLE);

                try {
                    Thread.sleep(SLEEP);
                }
                catch (InterruptedException e) {
                    // Ignore
                }
            }
        }

        public void kill()
        {
            alive = false;
        }
    }

    /*
     * Runnable for refreshing the HUD's UI.
     */
    private class UpdateUIRunnable implements Runnable
    {
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
                double speed = (int) (lastSpeed * 100.0) / 100.0;
                double turnrate = (int) (lastTurnrate * 100.0) / 100.0;

                if (speedView != null)
                    speedView.setText(String.format((String) getText(R.string.speed_string), speed));

                if (turnrateView != null)
                    turnrateView.setText(String.format((String) getText(R.string.turnrate_string), turnrate));

                if (locationView != null) {
                    Location loc = robotGPSNode.getLastKnownLocation();

                    if (loc != null) {
                        String strLongitude = Location.convert(loc.getLongitude(), Location.FORMAT_SECONDS);
                        String strLatitude = Location.convert(loc.getLatitude(), Location.FORMAT_SECONDS);

                        strLongitude = getLatLongString(strLongitude, false);
                        strLatitude = getLatLongString(strLatitude, true);

                        locationView.setText(String.format((String) getText(R.string.location_string),
                                strLatitude, strLongitude));
                    }
                }
                if (latView != null) {
                    Location loc = robotGPSNode.getLastKnownLocation();

                    if (loc != null) {
                        String strLatitude = Location.convert(loc.getLatitude(), Location.FORMAT_SECONDS);
                        strLatitude = getLatLongString(strLatitude, true);
                        latView.setText(strLatitude);
                    }
                }
                if (longView != null) {
                    Location loc = robotGPSNode.getLastKnownLocation();

                    if (loc != null) {
                        String strLongitude = Location.convert(loc.getLongitude(), Location.FORMAT_SECONDS);
                        strLongitude = getLatLongString(strLongitude, false);
                        longView.setText(strLongitude);
                    }
                }

                if (wifiStrengthView != null) {
                    wifiStrengthView.setImageResource(wifiIcons[lastWifiImage]);
                }

            } catch (IllegalStateException e) {
                // Ignore
            }
        }
    }
}
