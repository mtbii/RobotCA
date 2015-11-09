package com.robotca.ControlApp;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.FrameLayout;

import com.robotca.ControlApp.Fragments.CameraViewFragment;
import com.robotca.ControlApp.Fragments.JoystickFragment;
import com.robotca.ControlApp.Fragments.LaserScanFragment;
import com.robotca.ControlApp.Fragments.PreferencesFragment;

import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

public class ControlApp extends RosActivity {
    //private NodeMainExecutor nodeMainExecutor;
    private ControlAppActionTabListener tabListener;
    private FrameLayout frameLayout;
    private Fragment lastFrag;

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

        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        setContentView(R.layout.main);

        laserScanFragment = new LaserScanFragment();
        preferencesFragment = new PreferencesFragment();

        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //getFragmentManager().beginTransaction().add(R.id.joystick_holder, joystickFragment).commit();

        joystickFragment = (JoystickFragment) getFragmentManager().findFragmentById(R.id.joystick_fragment);
        cameraViewFragment = (CameraViewFragment) getFragmentManager().findFragmentById(R.id.camera_fragment);
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

            initRos(nodeMainExecutor, nodeConfiguration);
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
        joystickFragment.initialize(nodeMainExecutor, nodeConfiguration);
        laserScanFragment.initialize(nodeMainExecutor, nodeConfiguration);
        cameraViewFragment.initialize(nodeMainExecutor, nodeConfiguration);
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
