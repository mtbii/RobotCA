package com.robotca.ControlApp.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robotca.ControlApp.R;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.view.RosImageView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import sensor_msgs.CompressedImage;

/**
 * Created by Michael Brunson on 11/7/15.
 */
public class CameraViewFragment extends RosFragment {
    private RosImageView<sensor_msgs.CompressedImage> cameraView;

    public CameraViewFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera_view, null);
        cameraView = (RosImageView<sensor_msgs.CompressedImage>) view.findViewById(R.id.camera_fragment_camera_view);

        cameraView.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("edittext_camera_topic", getString(R.string.camera_topic)));
        cameraView.setMessageType(CompressedImage._TYPE);
        cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        if (nodeConfiguration != null)
            nodeMainExecutor.execute(cameraView, nodeConfiguration.setNodeName("android/fragment_camera_view"));

        return view;
    }

    @Override
    void shutdown() {
        nodeMainExecutor.shutdownNodeMain(cameraView);
    }
}