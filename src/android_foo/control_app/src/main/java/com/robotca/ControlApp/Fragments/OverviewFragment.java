package com.robotca.ControlApp.Fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;

import sensor_msgs.CompressedImage;

/**
 *
 * Created by Michael Brunson on 11/7/15.
 */
public class OverviewFragment extends RosFragment {

    private VisualizationView vizView;
    private RosImageView<sensor_msgs.CompressedImage> cameraView;

//    private HUDFragment hudFragment;
//
//    private static final String TAG = "OverviewFragment";

    public OverviewFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, null);
        vizView = (VisualizationView) view.findViewById(R.id.viz_view);

        List<Layer> layers = new ArrayList<>();
        layers.add(new LaserScanLayer(
                PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString("edittext_laser_scan_topic", getString(R.string.laser_scan_topic)),
                Float.parseFloat(PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString("edittext_laser_scan_detail", "1"))));

        //layers.add(new OccupancyGridLayer("/map"));
        layers.add(new RobotLayer("base_link"));
        vizView.onCreate(layers);

        vizView.init(nodeMainExecutor);

        //noinspection unchecked
        cameraView = (RosImageView) view.findViewById(R.id.camera_view);
        cameraView.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("edittext_camera_topic", getString(R.string.camera_topic)));
        cameraView.setMessageType(CompressedImage._TYPE);
        cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
//
//        joystickView = (JoystickView) view.findViewById(R.id.joystick_view);
//        joystickView.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("edittext_joystick_topic", getString(R.string.joy_topic)));

        nodeMainExecutor.execute(cameraView, nodeConfiguration.setNodeName("android/camera_view"));
//        nodeMainExecutor.execute(joystickView, nodeConfiguration.setNodeName("android/joystick_view"));
        nodeMainExecutor.execute(vizView, nodeConfiguration.setNodeName("android/viz_view"));

//        // Initialize HUD fragment
//        if (hudFragment == null)
//            hudFragment = new HUDFragment();
//        getFragmentManager().beginTransaction().replace(R.id.hud_fragment_placeholder, hudFragment).commit();
//        hudFragment.initialize(nodeMainExecutor, nodeConfiguration);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        vizView.getCamera().jumpToFrame("base_link");
//        vizView.getCamera().zoom(vizView.getCamera().getViewport().getWidth() / 2, vizView.getCamera().getViewport().getHeight() / 2, .5);
    }

    @Override
    public void shutdown(){
        nodeMainExecutor.shutdownNodeMain(cameraView);
//        nodeMainExecutor.shutdownNodeMain(joystickView);
        nodeMainExecutor.shutdownNodeMain(vizView);
    }
}
