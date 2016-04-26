package com.robotca.ControlApp.Core;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the loading and saving of RobotInfos from an Activity's shared preferences.
 *
 * Created by Michael Brunson on 2/2/16.
 */
public class RobotStorage {

    private static final String ROBOT_INFOS_KEY = "ROBOT_INFOS_KEY";

    private static List<RobotInfo> g_cRobotInfos = Lists.newArrayList();
    private static Gson m_oGson = new Gson();

    // Log tag String
    private static final String TAG = "RobotStorage";

    /**
     * Loads from the specified Activity's preferences.
     * @param activity The Activity
     */
    public static synchronized void load(Activity activity){
            SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
            String defaultJson = m_oGson.toJson(new ArrayList<RobotInfo>());

            String robotInfoJson = pref.getString(ROBOT_INFOS_KEY, defaultJson);
            Type listOfRobotInfoType = new TypeToken<List<RobotInfo>>() {
            }.getType();

        g_cRobotInfos = m_oGson.fromJson(robotInfoJson, listOfRobotInfoType);

        RobotInfo.resolveRobotCount(g_cRobotInfos);
    }

    /**
     * @return The list of loaded RobotInfos
     */
    public static synchronized List<RobotInfo> getRobots(){
        return g_cRobotInfos;
    }

    /**
     * Removes a loaded RobotInfo.
     * @param activity The Activity from which to remove
     * @param robot The RobotInfo to remove
     * @return True if the specified RobotInfo was removed, false otherwise
     */
    @SuppressWarnings("unused")
    public static synchronized boolean remove(Activity activity, RobotInfo robot){
        boolean removed = g_cRobotInfos.remove(robot);
        save(activity);
        return removed;
    }

    /**
     * Removes a loaded RobotInfo.
     * @param activity The Activity from which to remove
     * @param index The index of the RobotInfo to remove
     * @return True if the specified RobotInfo was removed, false otherwise
     */
    public static synchronized RobotInfo remove(Activity activity, int index){
        RobotInfo removed = g_cRobotInfos.remove(index);
        save(activity);
        return removed;
    }

    /**
     * Updates the specified RobotInfo in storage.
     * @param activity The Activity
     * @param robot The updated RobotInfo
     * @return True if the specified RobotInfo was updated, false otherwise
     */
    public static synchronized boolean update(Activity activity, RobotInfo robot) {
        boolean updated = false;

        for (int i = 0; i < g_cRobotInfos.size(); i++) {

            if (g_cRobotInfos.get(i).compareTo(robot) == 0) {

                Log.d(TAG, "Updating robotinfo at position " + i + ": " + robot);
                Log.d(TAG, robot.getOdometryTopic());

                g_cRobotInfos.set(i, robot);
                save(activity);
                updated = true;
                break;
            }
        }
        return updated;
    }

    /**
     * Adds a RobotInfo to the RobotStorage.
     * @param activity The Activity containing the RobotStorage
     * @param robot The new RobotInfo
     * @return True if the RobotInfo was successfully added, false otherwise
     */
    public static synchronized boolean add(Activity activity, RobotInfo robot){
        boolean added = g_cRobotInfos.add(robot);
        save(activity);
        return added;
    }

    /**
     * Saves the RobotStorage.
     * @param activity The Activity in which to save the RobotStorage
     */
    public static synchronized void save(Activity activity){
        String robotInfosJson = m_oGson.toJson(g_cRobotInfos);
        SharedPreferences.Editor editor = activity.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(ROBOT_INFOS_KEY, robotInfosJson);
        editor.apply();
    }
}
