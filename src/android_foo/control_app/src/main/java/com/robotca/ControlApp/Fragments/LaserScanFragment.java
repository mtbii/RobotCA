package com.robotca.ControlApp.Fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robotca.ControlApp.Layers.LaserScanLayer;
import com.robotca.ControlApp.R;

import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.RobotLayer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Michael Brunson on 11/7/15.
 */
public class LaserScanFragment extends RosFragment {
    private VisualizationView laserView;

    public LaserScanFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(savedInstanceState != null)
            return laserView;

        View view = inflater.inflate(R.layout.laser_scan_view, null);
        laserView = (VisualizationView) view.findViewById(R.id.laser_scan_fragment_viz_view);

        List<Layer> layers = new ArrayList<>();

        layers.add(new LaserScanLayer(
                PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString("edittext_laser_scan_topic", getString(R.string.laser_scan_topic)),
                Float.parseFloat(PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString("edittext_laser_scan_detail", "1"))));
        layers.add(new RobotLayer("base_link"));
        laserView.onCreate(layers);

        laserView.init(nodeMainExecutor);
        if (nodeConfiguration != null)
            nodeMainExecutor.execute(laserView, nodeConfiguration.setNodeName("android/laser_view"));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (laserView != null)
            laserView.getCamera().jumpToFrame("base_link");
        //laserView.getCamera().zoom(laserView.getCamera().getViewport().getWidth() / 2, laserView.getCamera().getViewport().getHeight() / 2, .5);

    }

    public void shutdown(){
    }
}