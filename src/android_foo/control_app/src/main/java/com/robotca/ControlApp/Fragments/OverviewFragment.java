package com.robotca.ControlApp.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robotca.ControlApp.R;
import com.robotca.ControlApp.Views.JoystickView;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.view.RosImageView;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.RobotLayer;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.ArrayList;
import java.util.List;

import sensor_msgs.CompressedImage;

/**
 * Created by Michael Brunson on 11/7/15.
 */
public class OverviewFragment extends RosFragment {

    private VisualizationView vizView;
    private RosImageView<sensor_msgs.CompressedImage> cameraView;
    private JoystickView joystickView;

    public OverviewFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, null);
        vizView = (VisualizationView) view.findViewById(R.id.viz_view);

        List<Layer> layers = new ArrayList<>();
        layers.add(new LaserScanLayer(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("edittext_laser_scan_topic", getString(R.string.laser_scan_topic))));
        //layers.add(new OccupancyGridLayer("/map"));
        layers.add(new RobotLayer("base_link"));
        vizView.onCreate(layers);

        cameraView = (RosImageView) view.findViewById(R.id.camera_view);
        cameraView.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("edittext_camera_topic", getString(R.string.camera_topic)));
        cameraView.setMessageType(CompressedImage._TYPE);
        cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        joystickView = (JoystickView) view.findViewById(R.id.joystick_view);
        joystickView.setTopicName(getString(R.string.joy_topic));

        vizView.init(nodeMainExecutor);

        nodeMainExecutor.execute(cameraView, nodeConfiguration.setNodeName("android/camera_view"));
        nodeMainExecutor.execute(joystickView, nodeConfiguration.setNodeName("android/joystick_view"));
        nodeMainExecutor.execute(vizView, nodeConfiguration.setNodeName("android/viz_view"));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        vizView.getCamera().jumpToFrame("base_link");
        //vizView.getCamera().zoom(vizView.getCamera().getViewport().getWidth() / 2, vizView.getCamera().getViewport().getHeight() / 2, .5);
    }

    @Override
    public void shutdown(){
        nodeMainExecutor.shutdownNodeMain(cameraView);
        nodeMainExecutor.shutdownNodeMain(joystickView);
        nodeMainExecutor.shutdownNodeMain(vizView);
    }
}
