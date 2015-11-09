package com.robotca.ControlApp.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robotca.ControlApp.R;

import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.RobotLayer;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.ArrayList;

/**
 * Created by Michael Brunson on 11/7/15.
 */
public class LaserScanFragment extends Fragment implements IRosInitializer {
    private VisualizationView laserView;

    public LaserScanFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(savedInstanceState != null)
            return laserView;

        View view = inflater.inflate(R.layout.laser_scan_view, container);
        laserView = (VisualizationView) view.findViewById(R.id.viz_view);

        ArrayList<Layer> layers = new ArrayList<Layer>();
        layers.add(new LaserScanLayer(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("edittext_laser_scan_topic", getString(R.string.laser_scan_topic))));
        //layers.add(new OccupancyGridLayer("/map"));
        layers.add(new RobotLayer("base_link"));
        laserView.onCreate(layers);

        return laserView;
    }



    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {

        laserView.init(nodeMainExecutor);
        laserView.getCamera().jumpToFrame("base_link");
        laserView.getCamera().zoom(laserView.getCamera().getViewport().getWidth() / 2, laserView.getCamera().getViewport().getHeight() / 2, .5);

        nodeMainExecutor.execute(laserView, nodeConfiguration.setNodeName("android/laser_view"));

    }
}
