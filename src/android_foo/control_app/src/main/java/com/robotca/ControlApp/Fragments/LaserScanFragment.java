package com.robotca.ControlApp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.R;
import com.robotca.ControlApp.Views.LaserScanView;

/**
 * Fragment for the LaserScanLayer.
 * <p/>
 * Created by Michael Brunson on 11/7/15.
 */
public class LaserScanFragment extends SimpleFragment {

    private LaserScanView laserScanView;

    @SuppressWarnings("unused")
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

        View view = inflater.inflate(R.layout.laser_scan_view, container, false);

        laserScanView = (LaserScanView) view.findViewById(R.id.laser_scan_renderer_view);

        (view.findViewById(R.id.recenter_laser_scan)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        laserScanView.getLaserScanRenderer().recenter();
                    }
                });

        (view.findViewById(R.id.clear_waypoints)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ControlApp) getActivity()).clearWaypoints();
                    }
                });

        return view;
    }
}