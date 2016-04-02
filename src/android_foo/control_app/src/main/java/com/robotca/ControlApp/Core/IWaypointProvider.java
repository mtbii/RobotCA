package com.robotca.ControlApp.Core;

import android.location.Location;

import org.ros.rosjava_geometry.Vector3;

/**
 * Interface for instances capable of providing waypoint paths.
 *
 * Created by Mike on 2/13/2016.
 */
public interface IWaypointProvider {

    /**
     * @return The next point in the path
     */
    Vector3 getDestination();

    /**
     * Sets the destination.
     * @param goal The point
     */
    @SuppressWarnings("unused")
    void setDestination(Vector3 goal);
}