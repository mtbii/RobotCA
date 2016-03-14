package com.robotca.ControlApp.Core;

import android.location.Location;
import android.util.Log;

import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import sensor_msgs.NavSatFix;

/**
 * Created by Kenneth Spear on 2/5/16.
 * Subscriber class to robot's GPS updates
 */

public class RobotGPSSub implements NodeMain, IMyLocationProvider {

    private Location mLocation = new Location("ros");
    private IMyLocationConsumer mMyLocatationComsumer;
    private Subscriber<NavSatFix> mSubscriber;

    public RobotGPSSub() {
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("ros_gps");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        // Connect to ROS node publishing GPS info
        mSubscriber = connectedNode.newSubscriber("/navsat/fix", NavSatFix._TYPE);
        mSubscriber.addMessageListener(new MessageListener<NavSatFix>() {
            @Override
            // For every message set the Lat & Long to the robots location
            public void onNewMessage(NavSatFix navSatFix) {
                mLocation.setLatitude(navSatFix.getLatitude());
                mLocation.setLongitude(navSatFix.getLongitude());

                try {
                    Thread.sleep(1000, 0);

                    if (mMyLocatationComsumer != null) {
                        // Update on location change
                        mMyLocatationComsumer.onLocationChanged(mLocation, RobotGPSSub.this);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    public void onShutdown(Node node) {
        if (mSubscriber != null)
            mSubscriber.shutdown();
    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }

    @Override
    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
        mMyLocatationComsumer = myLocationConsumer;
        return true;
    }

    @Override
    public void stopLocationProvider() {
        mMyLocatationComsumer = null;
    }

    @Override
    public Location getLastKnownLocation() {
        return mLocation;
    }

    /*
     * Sets the Location.
     */
    public void setLocation(Location location)
    {
        mLocation = location;
    }
}
