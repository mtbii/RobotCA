package com.robotca.ControlApp.Core;

import android.location.Location;
import android.util.Log;

import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.ros.message.MessageListener;

import java.util.ArrayList;

import sensor_msgs.NavSatFix;

/**
 * Provides Location updates to multiple subscribers.
 *
 * Created by Nathaniel Stone on 3/22/16.
 */
public class LocationProvider implements IMyLocationProvider, MessageListener<NavSatFix>{

    private static final String TAG = "LocationProvider";
    private ArrayList<IMyLocationConsumer> consumers;

    private final Location LOCATION;

    /**
     * Default Constructor.
     */
    public LocationProvider() {
        consumers = new ArrayList<>();

        LOCATION = new Location("ROS");
    }

    @Override
    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {

        return consumers.add(myLocationConsumer);
    }

    @Override
    public void stopLocationProvider() {
        if (consumers.size() == 1)
            consumers.clear();
        else
            Log.w(TAG, "Don't know which LocationProvider to stop!");
    }

    @Override
    public Location getLastKnownLocation() {
        return LOCATION;
    }

    @Override
    public void onNewMessage(NavSatFix navSatFix) {
        LOCATION.setLatitude(navSatFix.getLatitude());
        LOCATION.setLongitude(navSatFix.getLongitude());

        // Fire Consumer callbacks
        for (IMyLocationConsumer consumer: consumers) {
            consumer.onLocationChanged(LOCATION, this);
        }
    }
}
