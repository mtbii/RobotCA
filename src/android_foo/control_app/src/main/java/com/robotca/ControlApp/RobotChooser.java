package com.robotca.ControlApp;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.robotca.ControlApp.Core.RobotInfo;
import com.robotca.ControlApp.Core.RobotInfoAdapter;
import com.robotca.ControlApp.Core.RobotStorage;
import com.robotca.ControlApp.Dialogs.AddEditRobotDialogFragment;
import com.robotca.ControlApp.Dialogs.ConfirmDeleteDialogFragment;

import java.lang.annotation.Target;

/**
 * Activity for choosing a Robot with which to connect. The user can connect to a previously connected
 * robot or can create a new one.
 * <p/>
 * Created by Michael Brunson on 1/23/16.
 */
public class RobotChooser extends AppCompatActivity implements AddEditRobotDialogFragment.DialogListener, ConfirmDeleteDialogFragment.DialogListener {

    public static final String FIRST_TIME_LAUNCH_KEY = "FIRST_TIME_LAUNCH";
    private View mEmptyView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    private ShowcaseView showcaseView;
    private boolean addedRobot;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.robot_chooser);

        mEmptyView = findViewById(R.id.robot_empty_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.robot_recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mLayoutManager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.robot_chooser_toolbar);
        setSupportActionBar(toolbar);

        RobotStorage.load(this);

        mAdapter = new RobotInfoAdapter(this, RobotStorage.getRobots());

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkRobotList();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                checkRobotList();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                checkRobotList();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkRobotList();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkRobotList();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                checkRobotList();
            }
        });

        mRecyclerView.setAdapter(mAdapter);

        boolean isFirstLaunch = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(FIRST_TIME_LAUNCH_KEY, true);

        try {
            if (RobotStorage.getRobots().size() == 0 && isFirstLaunch) {
                //Show initial tutorial message
                showcaseView = new ShowcaseView.Builder(this)
                        .setTarget(new ToolbarActionItemTarget(toolbar, R.id.action_add_robot))
                        .setStyle(R.style.CustomShowcaseTheme2)
                        .setContentTitle("Add a Robot")
                        .setContentText("Let's get started! You can add a robot to connect to using this button. Try adding one now.")
                        .build();

                //Get ready to show tutorial message when user adds a robot
                setupNextTutorialMessage();


            } else {
                addedRobot = true;
            }
        } catch (Exception e) {
        }
    }

    private void setupNextTutorialMessage() {
        //Have to get a reference to the new robot's list item view AFTER
        //it shows up in the RecyclerView
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
                                    .setContentTitle("Connect")
                                    .setContentText("To connect to this robot, tap it's name.")
                                    .build();

                            addedRobot = true;

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
    protected void onStart() {
        super.onStart();
        checkRobotList();
    }

    /*
     * Refreshes the list of RobotInfo choices.
     */
    private void checkRobotList() {
//        if (RobotStorage.getRobots().isEmpty() && !addedRobot) {
//            mRecyclerView.setVisibility(View.GONE);
//            mEmptyView.setVisibility(View.VISIBLE);
//        } else {
//            mRecyclerView.setVisibility(View.VISIBLE);
//            mEmptyView.setVisibility(View.GONE);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_robot_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_robot:

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
    int getAdapterSize() {
        return mAdapter.getItemCount();
    }
}
