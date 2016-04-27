package com.robotca.ControlApp.Fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.R;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.view.RosImageView;
import org.ros.message.MessageListener;

import sensor_msgs.CompressedImage;

/**
 * Fragment containing a CameraView and a LaserScanView.
 *
 * Created by Michael Brunson on 11/7/15.
 */
public class OverviewFragment extends RosFragment {

    private View view;
    private TextView noCameraTextView;
    private RosImageView<sensor_msgs.CompressedImage> cameraView;
    private RobotController controller;

    @SuppressWarnings("unused")
    private static final String TAG = "OverviewFragment";

    public OverviewFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null)
        {
            view = inflater.inflate(R.layout.fragment_overview, container, false);
            noCameraTextView = (TextView)view.findViewById(R.id.noCameraTextView);

            LaserScanFragment laserScanFragment = new LaserScanFragment();
            getFragmentManager().beginTransaction().replace(
                    R.id.laser_scan_placeholder, laserScanFragment).commit();

            //noinspection unchecked
            cameraView = (RosImageView) view.findViewById(R.id.camera_view);
            cameraView.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("edittext_camera_topic", getString(R.string.camera_topic)));
            cameraView.setMessageType(CompressedImage._TYPE);
            cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());


            try {
                controller = ((ControlApp) getActivity()).getRobotController();
            }
            catch(Exception ignore){
            }

            if(controller != null){
                controller.setCameraMessageReceivedListener(new MessageListener<CompressedImage>() {
                    @Override
                    public void onNewMessage(CompressedImage compressedImage) {
                        if (compressedImage != null) {
                            controller.setCameraMessageReceivedListener(null);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    noCameraTextView.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });
            }
        }

        if (isInitialized()) {
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
