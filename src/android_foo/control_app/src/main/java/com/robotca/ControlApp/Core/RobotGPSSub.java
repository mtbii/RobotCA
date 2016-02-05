package com.robotca.ControlApp.Core;

import android.location.Location;

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
 * Created by kennethspear on 2/5/16.
 */
public class RobotGPSSub implements NodeMain, IMyLocationProvider {

    double rosLat, rosLong;
    private IMyLocationConsumer mMyLocatationComsumer;

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("ros_gps");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        Subscriber<NavSatFix> subscriber = connectedNode.newSubscriber("/navsat/fix", NavSatFix._TYPE);
        subscriber.addMessageListener(new MessageListener<NavSatFix>() {
            @Override
            public void onNewMessage(NavSatFix navSatFix) {
                rosLat = navSatFix.getLatitude();
                rosLong = navSatFix.getLongitude();

            }
        });

    }

    @Override
    public void onShutdown(Node node) {

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
        Location location = new Location("ros");
        location.setLatitude(rosLat);
        location.setLongitude(rosLong);
        return location;
    }
}
