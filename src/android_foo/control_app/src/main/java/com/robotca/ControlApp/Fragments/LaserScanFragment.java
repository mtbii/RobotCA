package com.robotca.ControlApp.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.Savable;
import com.robotca.ControlApp.R;
import com.robotca.ControlApp.Views.LaserScanView;

/**
 * Fragment for the LaserScanLayer.
 * <p/>
 * Created by Michael Brunson on 11/7/15.
 */
public class LaserScanFragment extends SimpleFragment implements Savable {

    private LaserScanView laserScanView;

    // Bundle id for if the camera angle is locked on the robot's heading
    private static final String CAMERA_ANGLE_LOCKED_BUNDLE_ID = "com.robotca.ControlApp.Fragments.cameraAngleLocked";

    private static boolean cameraAngleLocked = true;

    @SuppressWarnings("unused")
    private static final String TAG = "LaserScanFragment";

    /**
     * Default Constructor.
     */
    public LaserScanFragment() {
    }

    /**
     * Locks or unlocks the camera angle from following the Robot's heading.
     * @param lock True if the camera angle should match the Robot and false otherwise
     */
    public void lockAngle(boolean lock) {
        cameraAngleLocked = lock;
        laserScanView.getLaserScanRenderer().makeCameraAngleFollowRobot(lock);
    }

    /**
     * @return Whether the camera's angle is locked on the Robot's heading
     */
    public boolean angleLocked() {
        return laserScanView.getLaserScanRenderer().angleFollowsRobot();
    }

    /**
     * Inflates the Fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_laser_scan, container, false);

        laserScanView = (LaserScanView) view.findViewById(R.id.laser_scan_renderer_view);

        (view.findViewById(R.id.recenter_laser_scan)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {laserScanView.getLaserScanRenderer().recenter();}
                });

        (view.findViewById(R.id.clear_waypoints_button)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        laserScanView.getLaserScanRenderer().stopMovingWaypoint();
                        ((ControlApp) getActivity()).clearWaypoints();
                    }
                });

        (view.findViewById(R.id.clear_waypoints_button)).setEnabled(!getControlApp().getWaypoints().isEmpty());

        CheckBox checkBox = ((CheckBox) view.findViewById(R.id.lock_camera_checkbox));

        checkBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        lockAngle(isChecked);
                    }
                });

        checkBox.setChecked(cameraAngleLocked);
        lockAngle(cameraAngleLocked);

        return view;
    }

    /**
     * Load from a Bundle.
     *
     * @param bundle The Bundle
     */
    @Override
    public void load(@NonNull Bundle bundle) {

        Log.d(TAG, "loading from bundle");

        lockAngle(bundle.getBoolean(CAMERA_ANGLE_LOCKED_BUNDLE_ID, true));
    }

    /**
     * Save to a Bundle.
     *
     * @param bundle The Bundle
     */
    @Override
    public void save(@NonNull Bundle bundle) {

        Log.d(TAG, "saving to bundle");

        bundle.putBoolean(CAMERA_ANGLE_LOCKED_BUNDLE_ID, angleLocked());
    }
}