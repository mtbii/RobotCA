package com.robotca.ControlApp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robotca.ControlApp.Core.RobotInfo;
import com.robotca.ControlApp.Core.RobotInfoAdapter;
import com.robotca.ControlApp.Dialogs.ConfirmDeleteDialogFragment;
import com.robotca.ControlApp.Dialogs.AddEditRobotDialogFragment;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Michael Brunson on 1/23/16.
 */
public class RobotChooser extends AppCompatActivity implements AddEditRobotDialogFragment.DialogListener, ConfirmDeleteDialogFragment.DialogListener {
    private static final String ROBOT_INFOS_KEY = "ROBOT_INFOS_KEY";

    private View mEmptyView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Gson gson = new Gson();
    private ArrayList<RobotInfo> mRobotInfos;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.robot_chooser);
        mEmptyView = findViewById(R.id.robot_empty_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.robot_recycler_view);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        String defaultJson = gson.toJson(new ArrayList<RobotInfo>());

        String robotInfoJson = null;

        if(savedInstanceState != null) {
            robotInfoJson = savedInstanceState.getString(ROBOT_INFOS_KEY, null);
        }

        if(robotInfoJson == null) {
            robotInfoJson = pref.getString(ROBOT_INFOS_KEY, defaultJson);
        }

        Type listOfRobotInfoType = new TypeToken<ArrayList<RobotInfo>>(){}.getType();
        mRobotInfos = (ArrayList<RobotInfo>) gson.fromJson(robotInfoJson, listOfRobotInfoType);

        Toolbar toolbar = (Toolbar) findViewById(R.id.robot_chooser_toolbar);
        setSupportActionBar(toolbar);

        mAdapter = new RobotInfoAdapter(this, mRobotInfos);

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkRobotList();
    }

    private void checkRobotList() {
        if(mRobotInfos.isEmpty()){
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
        else{
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_robot_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        String robotInfosJson = gson.toJson(mRobotInfos);
        outState.putString(ROBOT_INFOS_KEY, robotInfosJson);
        super.onSaveInstanceState(outState, outPersistentState);

        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(ROBOT_INFOS_KEY, robotInfosJson);
        editor.commit();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String robotInfosJson = savedInstanceState.getString(ROBOT_INFOS_KEY, "[]");

        Type listOfRobotInfoType = new TypeToken<ArrayList<RobotInfo>>(){}.getType();
        mRobotInfos = gson.fromJson(robotInfosJson, listOfRobotInfoType);

        if(mRobotInfos == null) {
            mRobotInfos = new ArrayList<>();
        }
    }

    @Override
    public void onAddEditDialogPositiveClick(RobotInfo newRobotInfo, int position) {
        if(position >= 0 && position < mRobotInfos.size()){
            updateRobot(position, newRobotInfo);
        }
        else {
            addRobot(newRobotInfo);
        }
    }

    @Override
    public void onAddEditDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onConfirmDeleteDialogPositiveClick(int position, String name) {
        if(position >= 0 && position < mRobotInfos.size()) {
            removeRobot(position);
        }
    }

    @Override
    public void onConfirmDeleteDialogNegativeClick() {

    }

    private boolean addRobot(RobotInfo info) {

        mRobotInfos.add(info);

        String robotInfosJson = gson.toJson(mRobotInfos);
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(ROBOT_INFOS_KEY, robotInfosJson);
        editor.commit();

        mAdapter.notifyItemInserted(mRobotInfos.size() - 1);
        return true;
    }

    private void updateRobot(int position, RobotInfo newRobotInfo) {
        RobotInfo info = mRobotInfos.get(position);
        info.setName(newRobotInfo.getName());
        info.setMasterUri(newRobotInfo.getMasterUri());

        String robotInfosJson = gson.toJson(mRobotInfos);
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(ROBOT_INFOS_KEY, robotInfosJson);
        editor.commit();

        mAdapter.notifyItemChanged(position);
    }

    private RobotInfo removeRobot(int position) {
        RobotInfo removed = mRobotInfos.remove(position);

        if(removed != null){
            String robotInfosJson = gson.toJson(mRobotInfos);
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putString(ROBOT_INFOS_KEY, robotInfosJson);
            editor.commit();

            mAdapter.notifyItemRemoved(position);
        }

        return removed;
    }
}
