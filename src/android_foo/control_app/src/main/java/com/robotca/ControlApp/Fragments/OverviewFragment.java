package com.robotca.ControlApp.Fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robotca.ControlApp.R;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.view.RosImageView;

import sensor_msgs.CompressedImage;

/**
 *
 * Created by Michael Brunson on 11/7/15.
 */
public class OverviewFragment extends RosFragment {

    private View view;
    private LaserScanFragment laserScanFragment;
    private RosImageView<sensor_msgs.CompressedImage> cameraView;


    @SuppressWarnings("unused")
    private static final String TAG = "OverviewFragment";

    public OverviewFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null)
        {
            view = inflater.inflate(R.layout.fragment_overview, container, false);

            laserScanFragment = new LaserScanFragment();
            getFragmentManager().beginTransaction().replace(
                    R.id.laser_scan_placeholder, laserScanFragment).commit();

            //noinspection unchecked
            cameraView = (RosImageView) view.findViewById(R.id.camera_view);
            cameraView.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("edittext_camera_topic", getString(R.string.camera_topic)));
            cameraView.setMessageType(CompressedImage._TYPE);
            cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
        }

        if (isInitialized()) {
//            laserScanFragment.initialize(nodeMainExecutor, nodeConfiguration);
            nodeMainExecutor.execute(cameraView, nodeConfiguration.setNodeName("android/camera_view"));
        }

        return view;
    }

    @Override
    public void shutdown(){

        if (isInitialized()) {
            nodeMainExecutor.shutdownNodeMain(cameraView);
        }
    }
}
