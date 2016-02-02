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
    private View cameraView;

    public CameraViewFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(savedInstanceState != null)
            return cameraView;

        cameraView = inflater.inflate(R.layout.fragment_camera_view, container, false);
//        cameraView = (RosImageView<sensor_msgs.CompressedImage>) view.findViewById(R.id.camera_view);

        return cameraView;
    }

    @Override
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {

        RosImageView<sensor_msgs.CompressedImage> imgView = getImageView();

        imgView.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("edittext_camera_topic", getString(R.string.camera_topic)));
        imgView.setMessageType(CompressedImage._TYPE);
        imgView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        nodeMainExecutor.execute(imgView, nodeConfiguration.setNodeName("android/fragment_camera_view"));
    }

    /*
     * Returns the RosImageView<> on this View.
     */
    private RosImageView<sensor_msgs.CompressedImage> getImageView()
    {
        //noinspection unchecked
        return (RosImageView<sensor_msgs.CompressedImage>) cameraView.findViewById(R.id.camera_view);
    }
}
