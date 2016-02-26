package com.robotca.ControlApp.Core;

import android.support.annotation.NonNull;

import java.net.URI;
import java.util.UUID;

/**
 * Created by Michael Brunson on 1/23/16.
 */
public class RobotInfo implements Comparable<RobotInfo>{
    private static int robotCount = 1;
    private UUID id = UUID.randomUUID();
    private String name;
    private String masterUriString;

    private String joystickTopic;
    private String cameraTopic;
    private String laserTopic;

    public RobotInfo(){
        //id = UUID.randomUUID();
        name = "Robot" + robotCount++;
        masterUriString = "http://localhost:11311";
        joystickTopic = "/joy_teleop/cmd_vel";
        cameraTopic = "/image_raw/compressed";
        laserTopic = "/scan";
    }

    public RobotInfo(String mName, String mMasterUri) {
        this.name = mName;
        this.masterUriString = mMasterUri;
    }

    public RobotInfo(UUID id, String name, String masterUriString, String joystickTopic, String laserTopic, String cameraTopic) {
        this.id = id;
        this.name = name;
        this.masterUriString = masterUriString;
        this.joystickTopic = joystickTopic;
        this.cameraTopic = cameraTopic;
        this.laserTopic = laserTopic;
    }

    public UUID getId(){return id;}

    public void setId(UUID id){ this.id = id; }

    public String getJoystickTopic() {
        return joystickTopic;
    }

    public void setJoystickTopic(String joystickTopic) {
        this.joystickTopic = joystickTopic;
    }

    public String getCameraTopic() {
        return cameraTopic;
    }

    public void setCameraTopic(String cameraTopic) {
        this.cameraTopic = cameraTopic;
    }

    public String getLaserTopic() {
        return laserTopic;
    }

    public void setLaserTopic(String laserTopic) {
        this.laserTopic = laserTopic;
    }

    public String getMasterUri() {
        return masterUriString;
    }

    public void setMasterUri(String mMasterUri) {
        this.masterUriString = mMasterUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String mName) {
        this.name = mName;
    }

    public URI getUri(){
        return URI.create(getMasterUri());
    }

    @Override
    public int compareTo(@NonNull RobotInfo another) {
        if(this.getId() == null){
            return -1;
        }

//        if(another == null){
//            return 1;
//        }

        if(another.getId() == null){
            return 1;
        }

        return this.getId().compareTo(another.getId());
    }
}
