package com.robotca.ControlApp;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.app.Fragment;
import android.widget.AdapterView;
import android.widget.ListView;
//import android.app.FragmentManager;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.robotca.ControlApp.Core.RobotInfo;
import com.robotca.ControlApp.Core.RobotInfoAdapter;
import com.robotca.ControlApp.Core.RobotStorage;
import com.robotca.ControlApp.Dialogs.AddEditRobotDialogFragment;
import com.robotca.ControlApp.Dialogs.ConfirmDeleteDialogFragment;
import com.robotca.ControlApp.Core.DrawerItem;
import com.robotca.ControlApp.Core.NavDrawerAdapter;
import com.robotca.ControlApp.Fragments.AboutFragmentRobotChooser;
import com.robotca.ControlApp.Fragments.HelpFragment;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;


/**
 * Activity for choosing a Robot with which to connect. The user can connect to a previously connected
 * robot or can create a new one.
 * <p/>
 * Created by Michael Brunson on 1/23/16.
 */
public class RobotChooser extends AppCompatActivity implements AddEditRobotDialogFragment.DialogListener,
        ConfirmDeleteDialogFragment.DialogListener, ListView.OnItemClickListener {

    /** Key for whether this is the first time the app has been launched */
    public static final String FIRST_TIME_LAUNCH_KEY = "FIRST_TIME_LAUNCH";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    private ShowcaseView showcaseView;
    private Toolbar mToolbar;

    // Navigation drawer items
    private String[] mFeatureTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    // Variables for keeping track of Fragments
    private Fragment fragment = null;
    private FragmentManager fragmentManager;
    private int fragmentsCreatedCounter = 0;

    // Log tag String
    private static final String TAG = "RobotChooser";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.robot_chooser);

//        mEmptyView = findViewById(R.id.robot_empty_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.robot_recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mToolbar = (Toolbar) findViewById(R.id.robot_chooser_toolbar);
        setSupportActionBar(mToolbar);

        RobotStorage.load(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.profileDrawer);
        mFeatureTitles = getResources().getStringArray(R.array.chooser_titles); //Where you set drawer item titles
        mDrawerList = (ListView) findViewById(R.id.left_drawer2);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.drawer_open,
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
        mDrawerToggle.syncState();

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        //noinspection deprecation
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        // Icons for the navigation drawer
        int[] imgRes = new int[]{
                R.drawable.ic_android_black_24dp,
                R.drawable.ic_help_black_24dp,
                R.drawable.ic_info_outline_black_24dp
        };

        // Populate the navigation drawer
        List<DrawerItem> drawerItems = new ArrayList<>();

        for (int i = 0; i < mFeatureTitles.length; i++) {
            drawerItems.add(new DrawerItem(mFeatureTitles[i], imgRes[i]));
        }

        NavDrawerAdapter drawerAdapter = new NavDrawerAdapter(this,
                R.layout.nav_drawer_menu_item,
                drawerItems);

        mDrawerList.setAdapter(drawerAdapter);
        mDrawerList.setOnItemClickListener(this);

        // Adapter for creating the list of Robot options
        mAdapter = new RobotInfoAdapter(this, RobotStorage.getRobots());

        mRecyclerView.setAdapter(mAdapter);

        ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

        // Check whether this is the first time the app has been launched on this device
        final boolean isFirstLaunch = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(FIRST_TIME_LAUNCH_KEY, true);

        // Delay the initial tutorial a little bit
        // This makes sure the view gets a good reference to the UI layout positions
        Runnable task = new Runnable() {
            public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (RobotStorage.getRobots().size() == 0 && isFirstLaunch) {
                            //Show initial tutorial message
                            showcaseView = new ShowcaseView.Builder(RobotChooser.this)
                                .setTarget(new ToolbarActionItemTarget(mToolbar, R.id.action_add_robot))
                                .setStyle(R.style.CustomShowcaseTheme2)
                                .hideOnTouchOutside()
                                .blockAllTouches()
                                //.singleShot(0) Can use this instead of manually saving in preferences
                                .setContentTitle("Add a Robot")
                                .setContentText("Let's get started! You can add a robot to connect to using this button. Try adding one now.")
                                .build();

                            //Get ready to show tutorial message when user adds a robot
                            setupNextTutorialMessage();
                        }
                    } catch (Exception ignore) {}
                }
            });
            }
        };

        worker.schedule(task, 1, TimeUnit.SECONDS);
    }

    /*
     * Callback for when an item is selected from the NavigationDrawer.
     */
    private void selectItem(int position){
        Bundle args = new Bundle();
        fragmentManager = getSupportFragmentManager();

        switch (position) {
            case 0:
                fragmentsCreatedCounter = 0;

                if (fragment != null) {
                    fragmentManager.beginTransaction().remove(fragment).commit();
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

                mDrawerLayout.closeDrawers();
                return;

            case 1: // The Help Fragment
                fragment = new HelpFragment();
                fragment.setArguments(args);
                fragmentsCreatedCounter = fragmentsCreatedCounter + 1;
                mRecyclerView.setVisibility(View.GONE);

                // Insert the fragment by replacing any existing fragment
                if (fragment != null) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame2, fragment)
                            .commit();
                }
                break;

            case 2: // The About Fragment
                fragment = new AboutFragmentRobotChooser();
                fragment.setArguments(args);
                fragmentsCreatedCounter = fragmentsCreatedCounter + 1;
                mRecyclerView.setVisibility(View.GONE);

                // Insert the fragment by replacing any existing fragment
                if (fragment != null) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame2, fragment)
                            .commit();
                }
                break;

            default:
                break;
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(mFeatureTitles[position]);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {

        // Move to the previous fragment if it exists
        if (fragmentsCreatedCounter >= 1) {

            selectItem(0);
            fragmentsCreatedCounter=0;

        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (showcaseView != null){
            showcaseView.hide();
        }
    }

    /*
     * Sets up the showcase view for the second tutorial message.
     */
    private void setupNextTutorialMessage() {
        // Have to get a reference to the new robot's list item view AFTER
        // it shows up in the RecyclerView
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Wait until the RecyclerView has a child
                while (mRecyclerView.getChildCount() <= 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                View v = null;

                for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
                    v = mRecyclerView.getChildAt(i);
                    if (v != null) {
                        v = v.findViewById(R.id.robot_info_text);

                        if (v != null) break;
                    }
                }

                final View layoutView = v;

                if (layoutView != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showcaseView = new ShowcaseView.Builder(RobotChooser.this)
                                    .setTarget(new ViewTarget(layoutView))
                                    .setStyle(R.style.CustomShowcaseTheme2)
                                    .hideOnTouchOutside()
                                    .blockAllTouches()
                                    .setContentTitle("Connect")
                                    .setContentText("To connect to this robot, tap it's name.")
                                    .build();

                            PreferenceManager
                                    .getDefaultSharedPreferences(RobotChooser.this)
                                    .edit()
                                    .putBoolean(FIRST_TIME_LAUNCH_KEY, true)
                                    .commit();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_robot_chooser, menu);
        return true;
    }

    /**
     * Callback for when the user wants to add a new RobotItem.
     * @param item The selected MenuItem
     * @return True if the item selection was handled and false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_robot:

                // If add robot is pressed from a screen other than robot chooser, switch
                // back to chooser
                if (fragment != null) {
                    fragmentManager.beginTransaction().remove(fragment).commit();
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

                RobotInfo.resolveRobotCount(RobotStorage.getRobots());

                AddEditRobotDialogFragment addRobotDialogFragment = new AddEditRobotDialogFragment();
                addRobotDialogFragment.setArguments(null);
                addRobotDialogFragment.show(getSupportFragmentManager(), "addrobotdialog");

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAddEditDialogPositiveClick(RobotInfo newRobotInfo, int position) {
        if (position >= 0 && position < RobotStorage.getRobots().size()) {
            updateRobot(position, newRobotInfo);
        } else {
            addRobot(newRobotInfo);
        }
    }

    @Override
    public void onAddEditDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onConfirmDeleteDialogPositiveClick(int position, String name) {
        if (position >= 0 && position < RobotStorage.getRobots().size()) {
            removeRobot(position);
        }
    }

    @Override
    public void onConfirmDeleteDialogNegativeClick() {

    }

    /**
     * Adds a new RobotInfo.
     *
     * @param info The new RobotInfo
     * @return True if the RobotInfo was added successfully, false otherwise
     */
    public boolean addRobot(RobotInfo info) {
        RobotStorage.add(this, info);

        mAdapter.notifyItemInserted(RobotStorage.getRobots().size() - 1);

        return true;
    }

    /**
     * Updates the RobotInfo at the specified position.
     *
     * @param position     The position of the RobotInfo to update
     * @param newRobotInfo The updated RobotInfo
     */
    public void updateRobot(int position, RobotInfo newRobotInfo) {

        Log.d(TAG, "updateRobot at position " + position + ": " + newRobotInfo);

        RobotStorage.update(this, newRobotInfo);
        mAdapter.notifyItemChanged(position);
    }

    /**
     * Removes the RobotInfo at the specified position.
     *
     * @param position The position of the RobotInfo to remove
     * @return The removed RobotInfo if it existed
     */
    public RobotInfo removeRobot(int position) {
        RobotInfo removed = RobotStorage.remove(this, position);

        if (removed != null) {
            mAdapter.notifyItemRemoved(position);
        }

        if (RobotStorage.getRobots().size() == 0) {
            mAdapter.notifyDataSetChanged();
        }

        return removed;
    }

    /**
     * @return mAdapter item count.
     */
    @SuppressWarnings("unused")
    int getAdapterSize() {
        return mAdapter.getItemCount();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }
}
