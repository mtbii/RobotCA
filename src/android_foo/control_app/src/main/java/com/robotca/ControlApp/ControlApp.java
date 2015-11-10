package com.robotca.ControlApp;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.robotca.ControlApp.Fragments.CameraViewFragment;
import com.robotca.ControlApp.Fragments.JoystickFragment;
import com.robotca.ControlApp.Fragments.LaserScanFragment;
import com.robotca.ControlApp.Fragments.PreferencesFragment;
import com.robotca.ControlApp.Views.JoystickView;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.RosActivity;
import org.ros.android.view.RosImageView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import sensor_msgs.CompressedImage;

public class ControlApp extends RosActivity {
    //private NodeMainExecutor nodeMainExecutor;
    private ControlAppActionTabListener tabListener;
    private FrameLayout frameLayout;
    private Fragment lastFrag;

    private JoystickView joystick_view;
    private RosImageView<sensor_msgs.CompressedImage> camera_view;

    private LaserScanFragment laserScanFragment;
    private CameraViewFragment cameraViewFragment;
    private JoystickFragment joystickFragment;
    private PreferencesFragment preferencesFragment;

    public ControlApp() {
        super("Test App","Test App");
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
            return;


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        setContentView(R.layout.main);

        joystickFragment = new JoystickFragment();
        cameraViewFragment = new CameraViewFragment();
        laserScanFragment = new LaserScanFragment();
        preferencesFragment = new PreferencesFragment();

        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //getFragmentManager().beginTransaction().add(R.id.joystick_holder, joystickFragment).commit();

        //joystickFragment = (JoystickFragment) getFragmentManager().findFragmentById(R.id.joystick_fragment);
        //cameraViewFragment = (CameraViewFragment) getFragmentManager().findFragmentById(R.id.camera_fragment);
        camera_view = (RosImageView<sensor_msgs.CompressedImage>) findViewById(R.id.camera_view);
        camera_view.setTopicName(getString(R.string.camera_topic));
        camera_view.setMessageType(CompressedImage._TYPE);
        camera_view.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        joystick_view = (JoystickView) findViewById(R.id.joystick_view);
        joystick_view.setTopicName(getString(R.string.joy_topic));

        tabListener = new ControlAppActionTabListener();

        //frameLayout = (FrameLayout)findViewById(R.id.frame_layout_tab_content);

        ActionBar.Tab laserScanTab = getActionBar().newTab();
        laserScanTab.setText("Laser");
        laserScanTab.setTabListener(tabListener);

        ActionBar.Tab cameraTab = getActionBar().newTab();
        cameraTab.setText("Camera");
        cameraTab.setTabListener(tabListener);

        ActionBar.Tab settingsTab = getActionBar().newTab();
        settingsTab.setText("Settings");
        settingsTab.setTabListener(tabListener);

        getActionBar().addTab(laserScanTab);
        getActionBar().addTab(cameraTab);
        getActionBar().addTab(settingsTab);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {

        try {
            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();

            NodeConfiguration nodeConfiguration =
                    NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());

            nodeMainExecutor.execute(camera_view, nodeConfiguration.setNodeName("android/camera_view"));
            nodeMainExecutor.execute(joystick_view, nodeConfiguration.setNodeName("android/joystick_view"));

            //initRos(nodeMainExecutor, nodeConfiguration);
        } catch (Exception e) {
            // Socket problem
            Log.e("RobotCA", "socket error trying to get networking information from the master uri");
        }
    }

    public void setCurrentItem(int position){
        switch(position){
            case 0:
                setTabFragment(laserScanFragment);
                break;

            case 1:
                setTabFragment(cameraViewFragment);
                break;

            case 2:
                setTabFragment(preferencesFragment);
                break;

            default:
                setTabFragment(joystickFragment);
                break;
        }
    }

    public void setTabFragment(Fragment frag){
//        try {
//
//            FragmentTransaction transaction = getFragmentManager().beginTransaction();
//            transaction.replace(R.id.frame_layout_tab_content ,frag);
//            transaction.commit();
//
//            lastFrag = frag;
//        }
//        catch(Exception e) {
//
//        }
    }

    public void initRos(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration){
        //joystickFragment.initialize(nodeMainExecutor, nodeConfiguration);
        //laserScanFragment.initialize(nodeMainExecutor, nodeConfiguration);
        //cameraViewFragment.initialize(nodeMainExecutor, nodeConfiguration);
    }


    private class ControlAppActionTabListener implements ActionBar.TabListener{

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }
    }
}
