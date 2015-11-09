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
public class CameraViewFragment extends Fragment implements IRosInitializer {
    private RosImageView<sensor_msgs.CompressedImage> cameraView;

    public CameraViewFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(savedInstanceState != null)
            return cameraView;

        View view = inflater.inflate(R.layout.camera_view, container);
        cameraView = (RosImageView<sensor_msgs.CompressedImage>) view.findViewById(R.id.camera_view);
        return cameraView;
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {

        cameraView.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("edittext_camera_topic", getString(R.string.camera_topic)));
        cameraView.setMessageType(CompressedImage._TYPE);
        cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        nodeMainExecutor.execute(cameraView, nodeConfiguration.setNodeName("android/camera_view"));
    }
}
