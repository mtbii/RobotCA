package com.robotca.ControlApp.Core;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Brunson on 2/2/16.
 */
public class RobotStorage {
    private static final String ROBOT_INFOS_KEY = "ROBOT_INFOS_KEY";
    private static List<RobotInfo> g_cRobotInfos = Lists.newArrayList();
    private static Gson m_oGson = new Gson();

    public static synchronized void load(Activity activity){
            SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
            String defaultJson = m_oGson.toJson(new ArrayList<RobotInfo>());

            String robotInfoJson = pref.getString(ROBOT_INFOS_KEY, defaultJson);
            Type listOfRobotInfoType = new TypeToken<List<RobotInfo>>() {
            }.getType();

            g_cRobotInfos = (List<RobotInfo>) m_oGson.fromJson(robotInfoJson, listOfRobotInfoType);
    }

    public static synchronized List<RobotInfo> getRobots(){
        return g_cRobotInfos;
    }

    public static synchronized boolean remove(Activity activity, RobotInfo robot){
        boolean removed = g_cRobotInfos.remove(robot);
        save(activity);
        return removed;
    }

    public static synchronized RobotInfo remove(Activity activity, int index){
        RobotInfo removed = g_cRobotInfos.remove(index);
        save(activity);
        return removed;
    }

    public static synchronized boolean update(Activity activity, RobotInfo robot){
        boolean updated = false;
        for(int i = 0; i < g_cRobotInfos.size(); i++){
            if(g_cRobotInfos.get(i).compareTo(robot) == 0){
                g_cRobotInfos.set(i, robot);
                save(activity);
                updated = true;
                break;
            }
        }
        return updated;
    }

    public static synchronized boolean add(Activity activity, RobotInfo robot){
        boolean added = g_cRobotInfos.add(robot);
        save(activity);
        return added;
    }

    public static synchronized void save(Activity activity){
        String robotInfosJson = m_oGson.toJson(g_cRobotInfos);
        SharedPreferences.Editor editor = activity.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(ROBOT_INFOS_KEY, robotInfosJson);
        editor.commit();
    }
}
