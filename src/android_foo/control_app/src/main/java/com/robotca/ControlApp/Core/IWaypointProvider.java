package com.robotca.ControlApp.Core;

import android.location.Location;

/**
 * Created by Mike on 2/13/2016.
 */
public interface IWaypointProvider {
    Location getDestination();
}