package com.robotca.ControlApp.Fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Layers.LaserScanLayer;
import com.robotca.ControlApp.R;

import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.RobotLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for the LaserScanLayer.
 * <p/>
 * Created by Michael Brunson on 11/7/15.
 */
public class LaserScanFragment extends RosFragment {
    private VisualizationView laserView;
    private LaserScanLayer laserScanLayer;

    private static final String TAG = "LaserScanFragment";

    /**
     * Default Constructor.
     */
    public LaserScanFragment() {
    }

    /**
     * Inflates the Fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        if(savedInstanceState != null)
//            return laserView;

        View view = inflater.inflate(R.layout.laser_scan_view, container, false);

        laserView = (VisualizationView) view.findViewById(R.id.laser_scan_fragment_viz_view);

        List<Layer> layers = new ArrayList<>();

        layers.add(laserScanLayer = new LaserScanLayer(
                PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString("edittext_laser_scan_topic", getString(R.string.laser_scan_topic)),
                Float.parseFloat(PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString("edittext_laser_scan_detail", "1")), (ControlApp) getActivity()));
//        layers.add(new RobotLayer("base_link"));
        laserView.onCreate(layers);

        laserView.init(nodeMainExecutor);

        (view.findViewById(R.id.recenter_laser_scan)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        laserScanLayer.recenter();
                    }
                });

        (view.findViewById(R.id.clear_waypoints)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ControlApp) getActivity()).clearWaypoints();
                    }
                });

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

    @Override
    public void shutdown() {
    }
}