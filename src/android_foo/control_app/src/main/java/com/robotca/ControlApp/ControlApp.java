package com.robotca.ControlApp;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.ros.android.RosActivity;
import org.ros.android.view.RosTextView;
import org.ros.android.view.VirtualJoystickView;
import org.ros.android.view.camera.RosCameraPreviewView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

public class ControlApp extends RosActivity
{
    private int cameraId;
    private VirtualJoystickView virtualJoystick;

    public ControlApp() {
        super("Test App","Test App");

        Settings.setJoystickTopic(getString(R.string.joy_topic));
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
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {

        try {
            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();
            NodeConfiguration nodeConfiguration =
                    NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());

            virtualJoystick.setTopicName(Settings.getJoystickTopic());

            nodeMainExecutor.execute(virtualJoystick, nodeConfiguration.setNodeName("android/virtual_joystick"));
        } catch (Exception e) {
            // Socket problem
            Log.e("Teleop Tutorial", "socket error trying to get networking information from the master uri");
        }
    }
}
