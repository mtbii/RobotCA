package com.robotca.ControlApp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.R;

import org.ros.message.MessageListener;

import nav_msgs.Odometry;

/**
 * Simple fragment showing info about the Robot's current state.
 * Shows the robot's speed and direction, gps location, and signal strength.
 * Additionally, if the WarningSystem is enabled, the HUDFragment will flash red when the Robot is too
 * close to an obstacle.
 *
 * @author Nathaniel Stone
 */
public class HUDFragment extends SimpleFragment implements MessageListener<Odometry>{

    @SuppressWarnings("unused")
    private static final String TAG = "HUDFragment";

    private View view;
    private TextView speedView, turnrateView, latView, longView;
    private ImageView wifiStrengthView;

    private Button emergencyStopButton;

    // Updates this Fragments UI on the UI Thread
    private final UpdateUIRunnable UPDATE_UI_RUNNABLE = new UpdateUIRunnable();

    // Used for periodically querying WIFI strength and location info
    private final Updater UPDATER = new Updater();

    // Used for getting connection strength info
    private WifiManager wifiManager;

    private double lastSpeed, lastTurnrate;
    private int lastWifiImage;

    // Used for warning the user of collision
    private float warnAmount;
    private long lastWarn;
    private static final long WARN_DELAY = 100L;
    private static final float WARN_AMOUNT_INCR = 0.02f;
    private static final float WARN_AMOUNT_ATTEN = 0.75f;

    private long lastWarnTime;
    private static final long WARN_RATE = 10L;

    private boolean beepsEnabled;
    private final ToneGenerator toneGenerator;
    private long lastToneTime;
    private static final long TONE_DELAY = 300L;

    /** Warn amounts higher than this are considered dangerous */
    public static final float DANGER_WARN_AMOUNT = 0.3f;

    // Icons for indicating WIFI signal strength
    private static final int[] WIFI_ICONS;

    static {
        WIFI_ICONS = new int[] {
                R.drawable.wifi_0,
                R.drawable.wifi_1,
                R.drawable.wifi_2,
                R.drawable.wifi_3,
                R.drawable.wifi_4};
    }

    /**
     * Default Constructor.
     */
    public HUDFragment() {
        toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
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
        
        // Find the Emergency Stop Button
        // Emergency stop button
        if (emergencyStopButton == null) {
            try {
                //noinspection ConstantConditions
                emergencyStopButton = (Button) view.findViewById(R.id.emergency_stop_button);
                initEmergencyStopButton();
            }
            catch (NullPointerException e){
                // Ignore
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getControlApp());
        beepsEnabled = prefs.getBoolean(getString(R.string.prefs_warning_beep_key), true);

        // Start the Update
        new Thread(UPDATER).start();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        UPDATER.kill();

        toneGenerator.stopTone();
        toneGenerator.release();
    }

    /**
     * Callback for receiving odometry messages.
     * @param message The Odometry message
     */
    @Override
    public void onNewMessage(Odometry message) {

        updateUI(message.getTwist().getTwist().getLinear().getX(),
                message.getTwist().getTwist().getAngular().getZ());
    }

    /**
     * Toggles the appearance of the emergency stop button.
     * @param stop If true, the button will be red with STOP displayed, otherwise green with START displayed
     */
    @SuppressWarnings("deprecation") // the new version of getColor() is not compatible with the minimum API level
    public void toggleEmergencyStopUI(boolean stop) {

        if (stop) {
            emergencyStopButton.setText(R.string.stop);
            emergencyStopButton.setBackgroundColor(getResources().getColor(R.color.emergency_stop_red));
        } else {
            emergencyStopButton.setText(R.string.start);
            emergencyStopButton.setBackgroundColor(getResources().getColor(R.color.emergency_stop_green));
        }

        if (getControlApp().getControlMode().USER_CONTROLLED)
            emergencyStopButton.setBackgroundColor(getResources().getColor(R.color.emergency_stop_gray));
    }

    /**
     * 'Warns' the user, where multiple warnings will cause the HUD to turn red in appearance and flash.
     */
    public void warn()
    {
        if (System.currentTimeMillis() - lastWarnTime > WARN_RATE) {
            lastWarnTime = System.currentTimeMillis();

            warnAmount = Math.min(1.0f, warnAmount + WARN_AMOUNT_INCR);
            lastWarn = System.currentTimeMillis();

            if (beepsEnabled && warnAmount > DANGER_WARN_AMOUNT && lastWarn - lastToneTime > TONE_DELAY
                    && RobotController.getSpeed() > 0.01) {
                lastToneTime = lastWarn;

                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, (int) TONE_DELAY / 2);
            }
        }
    }

    /**
     * @return The warning 'amount,' in the range [0, 1] where higher numbers indicate higher warning levels
     */
    public float getWarnAmount() {
        return warnAmount;
    }

    /**
     * Enable/Disable warning beeps.
     * @param enabled Whether to enable or disable warning beeps
     */
    public void setBeepsEnabled(boolean enabled) {
        this.beepsEnabled = enabled;
    }

    /**
     * Updates this Fragment's speed and turnrate displays.
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
     * Initializes the EmergencyStopButton.
     */
    private void initEmergencyStopButton() {
        emergencyStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!getControlApp().getControlMode().USER_CONTROLLED) {
                    // Try to resume a paused plan first
                    if (getControlApp().getRobotController().resumePlan()) {
                        toggleEmergencyStopUI(true);
                    } else if (getControlApp().stopRobot(true)) {
                        toggleEmergencyStopUI(false);
                    }
                }

            }
        });
    }

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
         * Starts executing the active part of the class' code.
         */
        @Override
        public void run() {

            if (isDetached())
                return;

            try {
                // Basic experimentation has led me to conclude that speed values from the robot
                // are in m/s and turn rates in rad/s
                double speed = (int) (lastSpeed * 100.0) / 100.0;
                double turnrate = (int) (Math.toDegrees(lastTurnrate) * 100.0) / 100.0;

                // Update speed
                if (speedView != null)
                    speedView.setText(String.format((String) getText(R.string.speed_string), speed));

                // Update turn rate
                if (turnrateView != null)
                    turnrateView.setText(String.format((String) getText(R.string.turnrate_string), turnrate));

                // Update latitude display
                if (latView != null) {
                    Location location = ((ControlApp)getActivity()).getRobotController().
                            LOCATION_PROVIDER.getLastKnownLocation();

                    if (location != null) {
                        String strLatitude = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS);
                        strLatitude = getLatLongString(strLatitude, true);
                        latView.setText(strLatitude);
                    }
                }
                // Update longitude display
                if (longView != null) {
                    Location location = ((ControlApp)getActivity()).getRobotController().
                            LOCATION_PROVIDER.getLastKnownLocation();

                    if (location != null) {
                        String strLongitude = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS);
                        strLongitude = getLatLongString(strLongitude, false);
                        longView.setText(strLongitude);
                    }
                }

                // Update WIFI icons
                if (wifiStrengthView != null) {
                    wifiStrengthView.setImageResource(WIFI_ICONS[lastWifiImage]);
                }

                // Update warnings
                if (warnAmount > 0.0f)
                {
                    view.setBackgroundColor(getBackgroundColor());

                    if (System.currentTimeMillis() - lastWarn > WARN_DELAY) {
                        warnAmount *= WARN_AMOUNT_ATTEN;

                        if (warnAmount < 0.05f)
                            warnAmount = 0.0f;
                    }
                }

            } catch (IllegalStateException e) {
                // Ignore
            }
        }
    }

    /**
     * @return The background color based on the warning amount
     */
    private int getBackgroundColor()
    {
        final float p = (((System.currentTimeMillis() >> 7) & 1) == 0) ? warnAmount: 0.0f;
        final float q = 1.0f - p;

        return Color.argb(0xFF, (int)(p * 0xFF + q * 0xA0), (int)(q * 0xA0), (int)(q * 0xA0));
    }
}
