package com.robotca.ControlApp.Fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robotca.ControlApp.Layers.LaserScanLayer;
import com.robotca.ControlApp.R;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.view.RosImageView;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.RobotLayer;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.ArrayList;
import java.util.List;

import sensor_msgs.CompressedImage;

/**
 *
 * Created by Michael Brunson on 11/7/15.
 */
public class OverviewFragment extends RosFragment {

    private View view;
    private LaserScanFragment laserScanFragment;
    private RosImageView<sensor_msgs.CompressedImage> cameraView;


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
            laserScanFragment.initialize(nodeMainExecutor, nodeConfiguration);
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
