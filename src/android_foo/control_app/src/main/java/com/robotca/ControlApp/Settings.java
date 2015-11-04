package com.robotca.ControlApp;

/**
 * Created by Michael Brunson on 10/31/15.
 */

public class Settings {
    private static String JOYSTICK_TOPIC = "";

    public static String getJoystickTopic(){
        return JOYSTICK_TOPIC;
    }

    public static void setJoystickTopic(String val){
        JOYSTICK_TOPIC = val;
    }
}
