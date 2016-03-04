package com.robotca.ControlApp.Core;

import android.location.Location;

import org.ros.rosjava_geometry.Vector3;

/**
 * Created by Mike on 2/13/2016.
 */
public interface IWaypointProvider {
    Vector3 getDestination();
    void setDestination(Vector3 goal);
}