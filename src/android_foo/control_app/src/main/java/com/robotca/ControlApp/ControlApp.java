package com.robotca.ControlApp;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.robotca.ControlApp.Core.ControlMode;
import com.robotca.ControlApp.Core.DrawerItem;
import com.robotca.ControlApp.Core.IWaypointProvider;
import com.robotca.ControlApp.Core.NavDrawerAdapter;
import com.robotca.ControlApp.Core.Plans.RobotPlan;
import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.Core.RobotInfo;
import com.robotca.ControlApp.Core.RobotStorage;
import com.robotca.ControlApp.Core.Savable;
import com.robotca.ControlApp.Core.Utils;
import com.robotca.ControlApp.Core.WarningSystem;
import com.robotca.ControlApp.Fragments.AboutFragment;
import com.robotca.ControlApp.Fragments.CameraViewFragment;
import com.robotca.ControlApp.Fragments.HUDFragment;
import com.robotca.ControlApp.Fragments.JoystickFragment;
import com.robotca.ControlApp.Fragments.LaserScanFragment;
import com.robotca.ControlApp.Fragments.MapFragment;
import com.robotca.ControlApp.Fragments.OverviewFragment;
import com.robotca.ControlApp.Fragments.PreferencesFragment;
import com.robotca.ControlApp.Fragments.RosFragment;

import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.rosjava_geometry.Vector3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Main Activity for the App. The RobotController manages the connection with the Robot while this
 * class handles the UI.
 */
public class ControlApp extends RosActivity implements ListView.OnItemClickListener,
        IWaypointProvider, AdapterView.OnItemSelectedListener {

    /** Notification ticker for the App */
    public static final String NOTIFICATION_TICKER = "ROS Control";
    /** Notification title for the App */
    public static final String NOTIFICATION_TITLE = "ROS Control";

    /** The RobotInfo of the connected Robot */
    public static RobotInfo ROBOT_INFO;

    // Variables for managing the DrawerLayout
    private String[] mFeatureTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // NodeMainExecutor encapsulating the Robot's connection
    private NodeMainExecutor nodeMainExecutor;
    // The NodeConfiguration for the connection
    private NodeConfiguration nodeConfiguration;

    // Fragment for the Joystick
    private JoystickFragment joystickFragment;
    // Fragment for the HUD
    private HUDFragment hudFragment;

    // The RobotController for managing the connection to the Robot
    private RobotController controller;
    // The WarningSystem used for detecting imminent collisions
    private WarningSystem warningSystem;

    // Stuff for managing the current fragment
    private Fragment fragment = null;
    FragmentManager fragmentManager;
    int fragmentsCreatedCounter = 0;

    // For enabling/disabling the action menu
//    private boolean actionMenuEnabled = true;
    // The ActionBar spinner menu
    private Spinner actionMenuSpinner;

    // The index of the currently visible drawer
    private int drawerIndex = 1;

    // Log tag String
    private static final String TAG = "ControlApp";

    // List of waypoints
    private final LinkedList<Vector3> waypoints;
    // Specifies how close waypoints need to be to be considered touching
    private static final double MINIMUM_WAYPOINT_DISTANCE = 1.0;

//    // Laser scan map // static so that it doesn't need to be saved/loaded every time the screen rotates
//    private static LaserScanMap laserScanMap;

    // Bundle keys
    private static final String WAYPOINT_BUNDLE_ID = "com.robotca.ControlApp.waypoints";
    private static final String SELECTED_VIEW_NUMBER_BUNDLE_ID = "com.robotca.ControlApp.drawerIndex";
    private static final String CONTROL_MODE_BUNDLE_ID = "com.robotca.Views.Fragments.JoystickFragment.controlMode";

    // The saved instance state
    private Bundle savedInstanceState;

    /**
     * Default Constructor.
     */
    public ControlApp() {
        super(NOTIFICATION_TICKER, NOTIFICATION_TITLE, ROBOT_INFO.getUri());

        waypoints = new LinkedList<>();

//        // Create the laserScanMap
//        laserScanMap = new LaserScanMap();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Set default preference values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

        if (ROBOT_INFO != null) {
            ROBOT_INFO.save(editor);

//            editor.putString(getString(R.string.prefs_joystick_topic_edittext_key), ROBOT_INFO.getJoystickTopic());
//            editor.putString(getString(R.string.prefs_laserscan_topic_edittext_key), ROBOT_INFO.getLaserTopic());
//            editor.putString(getString(R.string.prefs_camera_topic_edittext_key), ROBOT_INFO.getCameraTopic());
//            editor.putString(getString(R.string.prefs_navsat_topic_edittext_key), ROBOT_INFO.getNavSatTopic());
//            editor.putString(getString(R.string.prefs_odometry_topic_edittext_key), ROBOT_INFO.getOdometryTopic());
//            editor.putString(getString(R.string.prefs_pose_topic_edittext_key), ROBOT_INFO.getPoseTopic());
        }

//        editor.putBoolean(getString(R.string.prefs_warning_checkbox_key), true);
//        editor.putBoolean(getString(R.string.prefs_warning_safemode_key), true);
//        editor.putBoolean(getString(R.string.prefs_warning_beep_key), true);

        editor.apply();

        // Set the main content view
        setContentView(R.layout.main);

        mFeatureTitles = getResources().getStringArray(R.array.feature_titles); // Where you set drawer item titles
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        if (getActionBar() != null) {
            ActionBar actionBar = getActionBar();

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);

            // Set custom Action Bar view
            LayoutInflater inflater = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.actionbar_dropdown_menu, null);

            actionMenuSpinner = (Spinner) v.findViewById(R.id.spinner_control_mode);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.motion_plans, android.R.layout.simple_spinner_item);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            actionMenuSpinner.setAdapter(adapter);
            actionMenuSpinner.setOnItemSelectedListener(this);

            actionBar.setCustomView(v);
            actionBar.setDisplayShowCustomEnabled(true);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
               /* R.drawable.ic_drawer,*/ R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        //noinspection deprecation
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        int[] imgRes = new int[]{
                R.drawable.ic_android_black_24dp,
                R.drawable.ic_view_quilt_black_24dp,
                R.drawable.ic_linked_camera_black_24dp,
                R.drawable.ic_navigation_black_24dp,
                R.drawable.ic_terrain_black_24dp,
                R.drawable.ic_settings_black_24dp,
                R.drawable.ic_info_outline_black_24dp
        };

        List<DrawerItem> drawerItems = new ArrayList<>();

        for (int i = 0; i < mFeatureTitles.length; i++) {
            drawerItems.add(new DrawerItem(mFeatureTitles[i], imgRes[i]));
        }

        NavDrawerAdapter drawerAdapter = new NavDrawerAdapter(this,
                R.layout.nav_drawer_menu_item,
                drawerItems);

        mDrawerList.setAdapter(drawerAdapter);
        mDrawerList.setOnItemClickListener(this);

        // Find the Joystick fragment
        joystickFragment = (JoystickFragment) getFragmentManager().findFragmentById(R.id.joystick_fragment);

        // Create the RobotController
        controller = new RobotController(this);

        // Hud fragment
        hudFragment = (HUDFragment) getFragmentManager().findFragmentById(R.id.hud_fragment);

        this.savedInstanceState = savedInstanceState;

        if (savedInstanceState != null) {
            //noinspection unchecked
            List<Vector3> list = (List<Vector3>) savedInstanceState.getSerializable(WAYPOINT_BUNDLE_ID);

            if (list != null) {
                waypoints.clear();
                waypoints.addAll(list);
                waypointsChanged();
            }

            setControlMode(ControlMode.values()[savedInstanceState.getInt(CONTROL_MODE_BUNDLE_ID)]);
            drawerIndex = savedInstanceState.getInt(SELECTED_VIEW_NUMBER_BUNDLE_ID);

            // Load the controller
            controller.load(savedInstanceState);
        }

        // Set the correct spinner item
        if (actionMenuSpinner != null)
        {
            actionMenuSpinner.setSelection(getControlMode().ordinal());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the Clear Waypoints button
        waypointsChanged();
    }

    @Override
    protected void onStop() {
        RobotStorage.update(this, ROBOT_INFO);

        Log.d(TAG, "onStop()");

        if (controller != null)
            controller.stop();

        if (joystickFragment != null)
            joystickFragment.stop();
        onTrimMemory(TRIM_MEMORY_BACKGROUND);
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy()");

        if (controller != null)
            controller.stop();

        if (joystickFragment != null)
            joystickFragment.stop();

        onTrimMemory(TRIM_MEMORY_BACKGROUND);
        onTrimMemory(TRIM_MEMORY_COMPLETE);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        // Save Waypoints
        bundle.putSerializable(WAYPOINT_BUNDLE_ID, waypoints);
        // Save Control Mode
        bundle.putInt(CONTROL_MODE_BUNDLE_ID, getControlMode().ordinal());
        // Save current drawer
        bundle.putInt(SELECTED_VIEW_NUMBER_BUNDLE_ID, drawerIndex);

        // Save the RobotController
        if (controller != null)
            controller.save(bundle);

        // Save the current fragment if applicable
        if (fragment instanceof Savable)
            ((Savable) fragment).save(bundle);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {

        try {
            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();

            this.nodeMainExecutor = nodeMainExecutor;
            this.nodeConfiguration =
                    NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    joystickFragment.invalidate();
                }
            });

            //controller.setTopicName(PreferenceManager.getDefaultSharedPreferences(this).getString("edittext_joystick_topic", getString(R.string.joy_topic)));
            controller.initialize(nodeMainExecutor, nodeConfiguration);

            // Add the HUDFragment to the RobotController's odometry listener
            controller.addOdometryListener(hudFragment);
            // Add the JoystickView to the RobotController's odometry listener
            controller.addOdometryListener(joystickFragment.getJoystickView());
            // Create and add a WarningSystem
            controller.addLaserScanListener(warningSystem = new WarningSystem(this));

//            // Add the LaserScanMap
//            controller.addLaserScanListener(laserScanMap);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View temp = mDrawerList.getChildAt(drawerIndex);
                    mDrawerList.onTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, temp.getX(), temp.getY(), 0));

                    selectItem(drawerIndex);
                }
            });
        } catch (Exception e) {
            // Socket problem
            Log.e(TAG, "socket error trying to get networking information from the master uri", e);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }

    /**
     * @return The RobotController
     */
    public RobotController getRobotController() {
        return controller;
    }

    /**
     * @return The HUDFragment
     */
    public HUDFragment getHUDFragment() {
        return hudFragment;
    }

    /**
     * @return The WarningSystem
     */
    public WarningSystem getWarningSystem() {
        return warningSystem;
    }

    /**
     * Called when a collision is imminent from the WarningSystem.
     */
    public void collisionWarning() {
//        Log.d(TAG, "Collision Warning!");

        hudFragment.warn();
    }

    /**
     * Call to stop the Robot.
     *
     * @param cancelMotionPlan Whether to cancel the current motion plan
     * @return True if a resumable RobotPlan was stopped
     */
    public boolean stopRobot(boolean cancelMotionPlan) {
        Log.d(TAG, "Stopping Robot");
        joystickFragment.stop();
        return controller.stop(cancelMotionPlan);
    }

//    /**
//     * @return The current laser scan map
//     */
//    public static LaserScanMap getLaserScanMap()
//    {
//        return laserScanMap;
//    }


//    /**
//     * Call to stop the Robot.
//     *
//     * @return True if a resumable RobotPlan was stopped
//     */
//    public boolean stopRobot() {
//        return stopRobot(true);
//    }

    /**
     * Locks/unlocks the screen orientation.
     * Adapted from an answer on StackOverflow by jp36
     *
     * @param lock Whether to lock the orientation
     */
    public void lockOrientation(boolean lock) {

        if (lock) {
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            int rotation = display.getRotation();
            int tempOrientation = getResources().getConfiguration().orientation;
            int orientation = 0;

            switch (tempOrientation) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    else
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Configuration.ORIENTATION_PORTRAIT:
                    if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    else
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
            }

            //noinspection ResourceType
            setRequestedOrientation(orientation);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    /*
     * Swaps fragments in the main content view.
     */
    private void selectItem(int position) {

        Bundle args = new Bundle();

        if (joystickFragment != null && getControlMode().ordinal() <= ControlMode.Tilt.ordinal()) {
            joystickFragment.show();
        }

        if (hudFragment != null) {
            hudFragment.show();
        }

        if (controller != null) {
            controller.initialize();
        }
        fragmentManager = getFragmentManager();

        setActionMenuEnabled(true);

        switch (position) {
            case 0:
                Log.d(TAG, "Drawer item 0 selected, finishing");

                fragmentsCreatedCounter = 0;

                int count = fragmentManager.getBackStackEntryCount();
                for (int i = 0; i < count; ++i) {
                    fragmentManager.popBackStackImmediate();
                }

                if (controller != null) {
                    controller.shutdownTopics();

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            nodeMainExecutor.shutdownNodeMain(controller);
                            return null;
                        }
                    }.execute();
                }

                finish();

                return;

            case 1:
                fragment = new OverviewFragment();
                fragmentsCreatedCounter = 0;
                break;

            case 2:
                fragment = new CameraViewFragment();
                fragmentsCreatedCounter = fragmentsCreatedCounter + 1;
                break;

            case 3:
                fragment = new LaserScanFragment();
                fragmentsCreatedCounter = fragmentsCreatedCounter + 1;
                break;

            case 4:
                fragment = new MapFragment();
                fragmentsCreatedCounter = fragmentsCreatedCounter + 1;
                break;

            case 5:
                if (joystickFragment != null)
                    joystickFragment.hide();
                if (hudFragment != null) {
                    hudFragment.hide();

                    boolean stop = controller.getMotionPlan() == null || !controller.getMotionPlan().isResumable();
                    stop &= !controller.hasPausedPlan();
                    hudFragment.toggleEmergencyStopUI(stop);
                }

                setActionMenuEnabled(false);
                stopRobot(false);

                fragment = new PreferencesFragment();
                fragmentsCreatedCounter = fragmentsCreatedCounter + 1;
                break;

            case 6:
                if (joystickFragment != null)
                    joystickFragment.hide();
                if (hudFragment != null) {
                    hudFragment.hide();

                    boolean stop = controller.getMotionPlan() == null || !controller.getMotionPlan().isResumable();
                    stop &= !controller.hasPausedPlan();
                    hudFragment.toggleEmergencyStopUI(stop);
                }

                setActionMenuEnabled(false);
                stopRobot(false);

                fragment = new AboutFragment();
                fragmentsCreatedCounter = fragmentsCreatedCounter + 1;

            default:
                break;
        }

        drawerIndex = position;

        try {
            //noinspection ConstantConditions
            ((RosFragment) fragment).initialize(nodeMainExecutor, nodeConfiguration);
        } catch (Exception e) {
            // Ignore
        }

        if (fragment != null) {
            fragment.setArguments(args);

            // Insert the fragment by replacing any existing fragment
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();

            if (fragment instanceof Savable && savedInstanceState != null)
                ((Savable) fragment).load(savedInstanceState);
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(mFeatureTitles[position]);

        // Refresh waypoints
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ignore) {}
                waypointsChanged();
                return null;
            }
        }.execute();
    }

    @Override
    public void setTitle(CharSequence title) {
        try {
            //noinspection ConstantConditions
            getActionBar().setTitle(title);
        } catch (NullPointerException e) {
            // Ignore
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        switch (item.getItemId()) {
            case R.id.action_joystick_control:
                setControlMode(ControlMode.Joystick);
                return true;

            case R.id.action_motion_control:
                setControlMode(ControlMode.Tilt);
                return true;

            case R.id.action_simple_waypoint_control:
                setControlMode(ControlMode.SimpleWaypoint);
                return true;

            case R.id.action_waypoint_control:
                setControlMode(ControlMode.Waypoint);
                return true;

            case R.id.action_random_walk_control:
                setControlMode(ControlMode.RandomWalk);
                return true;

            default:
                return mDrawerToggle.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        if (fragmentsCreatedCounter >= 1) {
            selectItem(1);
            fragmentsCreatedCounter = 0;
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//
//        for (int i = 0; i < menu.size(); i++) {
//            menu.getItem(i).setChecked(false);
//
//            if (i == 1)
//                menu.getItem(1).setEnabled(actionMenuEnabled && joystickFragment.hasAccelerometer());
//            else
//                menu.getItem(i).setEnabled(actionMenuEnabled);
//        }
//
//        menu.getItem(getControlMode().ordinal()).setChecked(true);
//
//        return true;
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_control_app, menu);
//        menu.getItem(0).setChecked(true);
//        return true;
//    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Called when the preferences may have changed.
     *
     * @param prefs The SharedPrefences object
     */
    public void onPreferencesChanged(SharedPreferences prefs) {

        // Warning System
        warningSystem.setEnabled(prefs.getBoolean(getString(R.string.prefs_warning_checkbox_key), true));
        warningSystem.enableSafemode(prefs.getBoolean(getString(R.string.prefs_warning_safemode_key), true));

        // Beep beep
        hudFragment.setBeepsEnabled(prefs.getBoolean(getString(R.string.prefs_warning_beep_key), true));

        // Refresh topic subscribers/publishers
        controller.refreshTopics();

    }

    /**
     * @return the Robot's current ControlMode
     */
    public ControlMode getControlMode() {
        return joystickFragment.getControlMode();
    }

    /**
     * Sets the ControlMode for controlling the Robot.
     *
     * @param controlMode The new ControlMode
     */
    public void setControlMode(ControlMode controlMode) {

        if (joystickFragment.getControlMode() == controlMode)
            return;

        // Lock the orientation for tilt controls
        lockOrientation(controlMode == ControlMode.Tilt);

        // Notify the Joystick on the new ControlMode
        joystickFragment.setControlMode(controlMode);
        hudFragment.toggleEmergencyStopUI(true);

        // If the ControlMode has an associated RobotPlan, run the plan
        RobotPlan robotPlan = ControlMode.getRobotPlan(this, controlMode);
        if (robotPlan != null) {
            controller.runPlan(robotPlan);
        } else {
            controller.stop();
        }

        invalidateOptionsMenu();

        if (controlMode == ControlMode.SimpleWaypoint || controlMode == ControlMode.Waypoint) {
            Toast.makeText(this, "Tap twice to place or delete a waypoint. " +
                    "Tap and hold a waypoint to move it.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Sets the destination point.
     *
     * @param location The point
     */
    public void setDestination(Vector3 location) {
        synchronized (waypoints) {
            waypoints.addFirst(location);
        }
        waypointsChanged();
    }

    /**
     * Adds a waypoint.
     *
     * @param point The point
     */
    public void addWaypoint(Vector3 point) {
        synchronized (waypoints) {
            waypoints.addLast(point);
        }
        waypointsChanged();
    }

    /**
     * Attempts to find a Waypoint at the specified position.
     *
     * @param point The position
     * @return The index of the Waypoint in the Waypoint list or -1 if no Waypoint was found at the point
     */
    public int findWaypointAt(Vector3 point, float scale) {
        // First find the nearest point
        double minDist = Double.MAX_VALUE, dist;
        Vector3 pt, near = null;
        int idx = -1;

        for (int i = 0; i < waypoints.size(); ++i) {

            pt = waypoints.get(i);
            dist = Utils.distanceSquared(point.getX(), point.getY(), pt.getX(), pt.getY());

            if (dist < minDist) {
                minDist = dist;
                near = pt;
                idx = i;
            }
        }

        if (near == null || minDist * scale >= MINIMUM_WAYPOINT_DISTANCE)
            idx = -1;

        return idx;
    }

    /**
     * Same as above but will remove a nearby way point if one is close instead of adding the new point.
     *
     * @param point The point
     * @param scale The camera scale
     */
    public void addWaypointWithCheck(Vector3 point, float scale) {

        // First find the nearest point
        double minDist = Double.MAX_VALUE, dist;
        Vector3 near = null;

        synchronized (waypoints) {
            for (Vector3 pt : waypoints) {
                dist = Utils.distanceSquared(point.getX(), point.getY(), pt.getX(), pt.getY());

                if (dist < minDist) {
                    minDist = dist;
                    near = pt;
                }
            }
        }

        if (near != null && minDist * scale < MINIMUM_WAYPOINT_DISTANCE) {

            final Vector3 remove = near;

            AlertDialog.Builder alert = new AlertDialog.Builder(ControlApp.this);
            alert.setTitle("Delete Waypoint");
            alert.setMessage("Are you sure you wish to delete this way point?");
            alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    synchronized (waypoints) {
                        waypoints.remove(remove);
                    }
                    waypointsChanged();

                    dialog.dismiss();
                }
            });
            alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });

            alert.show();

        } else {

            minDist = Double.MAX_VALUE;
            int j = -1;

            // See if the waypoint is on an existing line segment and if so insert it
            for (int i = 0; i < waypoints.size() - 1; ++i) {
                dist = Utils.distanceToLine(point.getX(), point.getY(), waypoints.get(i), waypoints.get(i + 1));

                if (dist < minDist) {
                    minDist = dist;
                    j = i;
                }
            }

            // Insert the waypoint if it is between two other waypoints
            if (minDist * scale < MINIMUM_WAYPOINT_DISTANCE) {
                synchronized (waypoints) {
                    waypoints.add(j + 1, point);
                }
                waypointsChanged();
            } else {
                addWaypoint(point);
            }
        }
    }

    /**
     * @return The next waypoint in line
     */
    @Override
    public Vector3 getDestination() {
        return waypoints.peekFirst();
    }

    /**
     * @return The next waypoint in line and removes it
     */
    public Vector3 pollDestination() {

        Vector3 r;

        synchronized (waypoints) {
            r = waypoints.pollFirst();
        }
        waypointsChanged();

        return r;
    }

    /**
     * @return The list of waypoints.
     */
    public LinkedList<Vector3> getWaypoints() {
        return waypoints;
    }

    /**
     * Clears all waypoints.
     */
    public void clearWaypoints() {
        synchronized (waypoints) {
            waypoints.clear();
        }
        waypointsChanged();
    }

    /*
     * Called when the waypoints have been edited.
     */
    private void waypointsChanged() {
        // Enable/Disable the clear waypoints button
        final View view;

        if (fragment != null && fragment.getView() != null
                && (view = fragment.getView().findViewById(R.id.clear_waypoints_button)) != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setEnabled(!waypoints.isEmpty());
                }
            });
        }
    }


    /**
     * Enables/disables the action bar menu.
     * @param enabled Whether to enable or disable the menu
     */
    public void setActionMenuEnabled(boolean enabled)
    {
//        actionMenuEnabled = enabled;
//        invalidateOptionsMenu();
        if (actionMenuSpinner != null)
            actionMenuSpinner.setEnabled(enabled);
    }

    /**
     * Callback for when a Spinner item is selected from the ActionBar.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setControlMode(ControlMode.values()[position]);
    }

    /**
     * Callback for when a Spinner item is selected from the ActionBar.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
