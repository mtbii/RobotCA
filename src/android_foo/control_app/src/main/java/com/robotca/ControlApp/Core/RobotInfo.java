package com.robotca.ControlApp.Core;

import android.support.annotation.NonNull;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Container for information about connections to specific Robots.
 *
 * Created by Michael Brunson on 1/23/16.
 */
public class RobotInfo implements Comparable<RobotInfo> {

    // Number of RobotInfos in storage
    private static int robotCount = 1;

    // UUID for this RobotInfo
    private UUID id = UUID.randomUUID();

    // Name of this RobotInfo
    private String name;
    // Master URI of this RobotInfo
    private String masterUriString;

    // Topic names
    private String joystickTopic;
    private String cameraTopic;
    private String laserTopic;
    private String navsatTopic;
    private String odometryTopic;
    private String poseTopic;

    @SuppressWarnings("unused")
    private static final String TAG = "RobotInfo";

    /**
     * Default Constructor.
     */
    public RobotInfo() {
        //id = UUID.randomUUID();
        name = "Robot" + robotCount++;
        masterUriString = "http://localhost:11311";
        joystickTopic = "/joy_teleop/cmd_vel";
        cameraTopic = "/image_raw/compressed";
        laserTopic = "/scan";
        navsatTopic = "/navsat/fix";
        odometryTopic = "/odometry/filtered";
        poseTopic = "/pose/";
    }

//    public RobotInfo(String mName, String mMasterUri) {
//        this.name = mName;
//        this.masterUriString = mMasterUri;
//    }

    /**
     * Creates a RobotInfo.
     * @param id UUID
     * @param name Name to show when displaying this RobotInfo
     * @param masterUriString Master URI for this RobotInfo
     * @param joystickTopic JoystickTopic name for this RobotInfo
     * @param laserTopic LaserTopic name for this RobotInfo
     * @param cameraTopic CameraTopic name for this RobotInfo
     */
    public RobotInfo(UUID id, String name, String masterUriString, String joystickTopic,
                     String laserTopic, String cameraTopic,String navsatTopic, String odometryTopic, String poseTopic) {
        this.id = id;
        this.name = name;
        this.masterUriString = masterUriString;
        this.joystickTopic = joystickTopic;
        this.laserTopic = laserTopic;
        this.cameraTopic = cameraTopic;
        this.navsatTopic = navsatTopic;
        this.odometryTopic = odometryTopic;
        this.poseTopic = poseTopic;
    }

    /**
     * @return UUID of this RobotInfo
     */
    public UUID getId(){return id;}

    /**
     * Sets the UUID of this RobotInfo
     * @param id The new UUID
     */
    public void setId(UUID id){ this.id = id; }

    /**
     * @return The PoseTopic name of this RobotInfo
     */
    public String getPoseTopic() {
        return poseTopic;
    }

    /**
     * Sets the PoseTopic for this RobotInfo.
     * @param poseTopic The new PoseTopic
     */
    public void setPoseTopic(String poseTopic) {
        this.poseTopic = poseTopic;
    }
    
     /**
     * @return The OdometryTopic name of this RobotInfo
     */
    public String getOdometryTopic() {
        return odometryTopic;
    }

    /**
     * Sets the OdometryTopic for this RobotInfo.
     * @param odometryTopic The new JoystickTopic
     */
    public void setOdometryTopic(String odometryTopic) {
        this.odometryTopic = odometryTopic;
    }

     /**
     * @return The NavSatTopic name of this RobotInfo
     */
    public String getNavSatTopic() {
        return navsatTopic;
    }

    /**
     * Sets the NavSatTopic for this RobotInfo.
     * @param navsatTopic The new JoystickTopic
     */
    public void setNavSatTopic(String navsatTopic) {
        this.navsatTopic = navsatTopic;
    }

    /**
     * @return The JoystickTopic name of this RobotInfo
     */
    public String getJoystickTopic() {
        return joystickTopic;
    }

    /**
     * Sets the JoystickTopic for this RobotInfo.
     * @param joystickTopic The new JoystickTopic
     */
    public void setJoystickTopic(String joystickTopic) {
        this.joystickTopic = joystickTopic;
    }

    /**
     * @return The CameraTopic of this RobotInfo
     */
    public String getCameraTopic() {
        return cameraTopic;
    }

    /**
     * Sets the CameraTopic of this RobotInfo.
     * @param cameraTopic The new CameraTopic
     */
    public void setCameraTopic(String cameraTopic) {
        this.cameraTopic = cameraTopic;
    }

    /**
     * @return The LaserTopic of this RobotInfo
     */
    public String getLaserTopic() {
        return laserTopic;
    }

    /**
     * Sets the LaserTopic of this RobotInfo.
     * @param laserTopic The new LaserTopic
     */
    public void setLaserTopic(String laserTopic) {
        this.laserTopic = laserTopic;
    }

    /**
     * @return The Master URI of this RobotInfo
     */
    public String getMasterUri() {
        return masterUriString;
    }

    /**
     * Sets the Master URI of this RobotInfo.
     * @param mMasterUri The new Master URI
     */
    public void setMasterUri(String mMasterUri) {
        this.masterUriString = mMasterUri;
    }

    /**
     * @return The name of this RobotInfo
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this RobotInfo.
     * @param mName The new name
     */
    public void setName(String mName) {
        this.name = mName;
    }

    /**
     * @return The URI of Master URI of this RobotInfo
     */
    public URI getUri(){
        return URI.create(getMasterUri());
    }

    /**
     * Compares this RobotInfo to another based on UUID.
     * @param another The other RobotInfo
     * @return The comparison result
     */
    @Override
    public int compareTo(@NonNull RobotInfo another) {

        if (this.getId() == null) {
            return -1;
        }

        if (another.getId() == null) {
            return 1;
        }

        return this.getId().compareTo(another.getId());
    }

    /**
     * Determines the correct value for robotCount.
     * @param list The list of loaded RobotInfos
     */
    public static void resolveRobotCount(List<RobotInfo> list)
    {
//        Log.d(TAG, "resolveRobotCount(" + list + ")");

        int max = 0;
        int val;

        for (RobotInfo info: list) {
            if (info.getName().startsWith("Robot"))
            {
//                Log.d(TAG, "name = " + info.getName().substring(5));
                try {
                    val = Integer.parseInt(info.getName().substring(5));
                }
                catch (NumberFormatException e) {
                    val = -1;
                }

                if (val > max)
                    max = val;
            }
        }

        robotCount = max + 1;
    }

    /**
     * @return The robot count.
     */
    @SuppressWarnings("unused")
    static int getRobotCount()
    {
        return robotCount;
    }
}
