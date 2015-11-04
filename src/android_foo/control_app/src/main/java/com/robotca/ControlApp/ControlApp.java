package com.robotca.ControlApp;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.BitmapFromImage;
import org.ros.android.RosActivity;
import org.ros.android.view.RosImageView;
import org.ros.android.view.RosTextView;
import org.ros.android.view.VirtualJoystickView;
import org.ros.android.view.camera.RosCameraPreviewView;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.OccupancyGridLayer;
import org.ros.android.view.visualization.layer.RobotLayer;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.ArrayList;

import sensor_msgs.CompressedImage;
import sensor_msgs.Image;

public class ControlApp extends RosActivity
{
    private int cameraId;
    private VirtualJoystickView virtualJoystick;
    private VisualizationView laserView;
    private RosImageView<sensor_msgs.CompressedImage> cameraView;

    public ControlApp() {
        super("Test App","Test App");
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        virtualJoystick = (VirtualJoystickView) findViewById(R.id.virtual_joystick);
        laserView = (VisualizationView) findViewById(R.id.viz_view);

        cameraView = (RosImageView<sensor_msgs.CompressedImage>) findViewById(R.id.camera_view);
        cameraView.setTopicName("/image_raw/compressed");
        cameraView.setMessageType(CompressedImage._TYPE);
        cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        ArrayList<Layer> layers = new ArrayList<Layer>();
        layers.add(new LaserScanLayer("/scan"));
        layers.add(new OccupancyGridLayer("/map"));
        layers.add(new RobotLayer("base_link"));

        laserView.onCreate(layers);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {

        try {
            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();
            NodeConfiguration nodeConfiguration =
                    NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());

            virtualJoystick.setTopicName(getString(R.string.joy_topic));
            laserView.init(nodeMainExecutor);
            laserView.getCamera().jumpToFrame("base_link");
            laserView.getCamera().zoom(laserView.getCamera().getViewport().getWidth() / 2, laserView.getCamera().getViewport().getHeight()/2, .5);

            nodeMainExecutor.execute(virtualJoystick, nodeConfiguration.setNodeName("android/virtual_joystick"));
            nodeMainExecutor.execute(laserView, nodeConfiguration.setNodeName("android/laser_view"));
            nodeMainExecutor.execute(cameraView, nodeConfiguration.setNodeName("android/camera_view"));
        } catch (Exception e) {
            // Socket problem
            Log.e("Teleop Tutorial", "socket error trying to get networking information from the master uri");
        }
    }
}
