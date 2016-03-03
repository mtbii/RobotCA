package com.robotca.ControlApp.Core;

/**
 * Enum for different ways to control the Robot.
 *
 * Created by Michael Brunson on 2/12/16.
 */
public enum ControlMode {
    Joystick, // Joystick control
    Motion, // Tilt sensor control
    Waypoint, // Waypoint control
    RandomWalk // Random walk
}
