package com.robotca.ControlApp.Fragments;

import android.content.Context;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.R;

import org.ros.message.MessageListener;

import nav_msgs.Odometry;

/**
 * Simple fragment showing info about the Robot's current state.
 *
 * @author Nathaniel Stone
 */
public class HUDFragment extends SimpleFragment implements MessageListener<Odometry>{

    @SuppressWarnings("unused")
    private static final String TAG = "HUDFragment";

    private View view;
    private TextView speedView, turnrateView, /*locationView,*/ latView, longView;
    private ImageView wifiStrengthView;

    private Button emergencyStopButton;

    // Updates this Fragments UI on the UI Thread
    private final UpdateUIRunnable UPDATE_UI_RUNNABLE = new UpdateUIRunnable();

    // Used for periodically querying WIFI strength and location info
    private final Updater UPDATER = new Updater();

    // Used for getting connection strength info
    private WifiManager wifiManager;

    private static double lastSpeed, lastTurnrate;
    private int lastWifiImage;

    private static int[] wifiIcons;

    /**
     * Default Constructor.
     */
    public HUDFragment() {
//        location = new Location("ros");
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

        // Get WifiManager
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        wifiIcons = new int[] {
                R.drawable.wifi_0,
                R.drawable.wifi_1,
                R.drawable.wifi_2,
                R.drawable.wifi_3,
                R.drawable.wifi_4};
        
        // Find the Emergency Stop Button
        // Emergency stop button
        if (emergencyStopButton == null) {
            try {
                //noinspection ConstantConditions
                emergencyStopButton = (Button) view.findViewById(R.id.emergency_stop_button);
                emergencyStopButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ControlApp)getActivity()).stopRobot();
                    }
                });
            }
            catch(NullPointerException e){
                // Ignore
            }
        }

        // Start the Update
        new Thread(UPDATER).start();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UPDATER.kill();
    }

//    @Override
//    public void initialize(NodeMainExecutor mainExecutor, NodeConfiguration nodeConfiguration)
//    {
//        super.initialize(mainExecutor, nodeConfiguration);
//
//        if (!isSetup) {
//            isSetup = true;
//            nodeMainExecutor.execute(robotGPSNode, nodeConfiguration.setNodeName("android/ros_gps"));
//        }
//
//        // Start the Update
//        new Thread(UPDATER).start();
//    }

//    public RobotGPSSub getRobotGPSNode() {
//        return robotGPSNode;
//    }

    /**
     * Callback for receiving odometry messages.
     * @param message The Odometry message
     */
    @Override
    public void onNewMessage(Odometry message) {
//            Log.d(TAG, "New Message: " + message.getTwist().getTwist().getLinear().getX());

//        // Record position
//        if (startPos == null) {
//            startPos = message.getPose().getPose().getPosition();
//        } else {
//            currentPos = message.getPose().getPose().getPosition();
//        }
//        rotation = message.getPose().getPose().getOrientation();

        updateUI(message.getTwist().getTwist().getLinear().getX(),
                message.getTwist().getTwist().getAngular().getZ());
    }

//    /**
//     * Shuts down the GPS Node
//     */
//    @Override
//    public void shutdown(){
//
//        if (isInitialized()) {
//            nodeMainExecutor.shutdownNodeMain(robotGPSNode);
//        }
//
//        UPDATER.kill();
//    }

    /**
     * Updates this Fragment's speed and turnrate displays
     */
    void updateUI(final double speed, final double turnrate)
    {
        if (!isDetached()) {
            lastSpeed = speed;
            lastTurnrate = turnrate;

            view.post(UPDATE_UI_RUNNABLE);
        }
    }

//    /**
//     * @return The Robot's GPS node
//     */
//    RobotGPSSub getGPSSub()
//    {
//        return robotGPSNode;
//    }

    /**
     * Formats a latitude/longitude String returned by Location.convert().
     * @param str The String
     * @param lat True if the String represents a latitude and false for longitude
     * @return The formatted String
     */
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

//    /**
//     * @return The Robot's x position
//     */
//    public static double getX() {
//        if (currentPos == null)
//            return 0.0;
//        else
//            return currentPos.getX() - startPos.getX();
//    }
//
//    /**
//     * @return The Robot's y position
//     */
//    public static double getY() {
//        if (currentPos == null)
//            return 0.0;
//        else
//            return currentPos.getY() - startPos.getY();
//    }
//
//    /**
//     * @return The Robot's heading in radians
//     */
//    public static double getHeading() {
//        if (rotation == null)
//            return 0.0;
//        else
//            return Utils.getHeading(org.ros.rosjava_geometry.Quaternion.fromQuaternionMessage(rotation));
//    }

    /**
     * @return The Robot's last reported speed
     */
    public static double getSpeed() {
        return lastSpeed;
    }

    /**
     * @return The Robot's last reported turn rate
     */
    public static double getTurnRate() {
        return lastTurnrate;
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

//                if (locationView != null) {
//                    Location location = ((ControlApp)getActivity()).getRobotController().
//                            LOCATION_PROVIDER.getLastKnownLocation();
//
//                    if (location != null) {
//                        String strLongitude = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS);
//                        String strLatitude = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS);
//
//                        strLongitude = getLatLongString(strLongitude, false);
//                        strLatitude = getLatLongString(strLatitude, true);
//
//                        locationView.setText(String.format((String) getText(R.string.location_string),
//                                strLatitude, strLongitude));
//                    }
//                }
                if (latView != null) {
                    Location location = ((ControlApp)getActivity()).getRobotController().
                            LOCATION_PROVIDER.getLastKnownLocation();

                    if (location != null) {
                        String strLatitude = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS);
                        strLatitude = getLatLongString(strLatitude, true);
                        latView.setText(strLatitude);
                    }
                }
                if (longView != null) {
                    Location location = ((ControlApp)getActivity()).getRobotController().
                            LOCATION_PROVIDER.getLastKnownLocation();

                    if (location != null) {
                        String strLongitude = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS);
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
