package com.robotca.ControlApp.Core.Plans;

import android.location.Location;

import com.robotca.ControlApp.Core.IWaypointProvider;
import com.robotca.ControlApp.Core.RobotController;

import org.ros.rosjava_geometry.Quaternion;
import org.ros.rosjava_geometry.Vector3;

import nav_msgs.Odometry;
import sensor_msgs.LaserScan;
import sensor_msgs.NavSatFix;

/**
 * Created by Mike on 2/13/2016.
 */
public class WaypointPlan extends RobotPlan {
    private IWaypointProvider provider;

    private Location lastLocation;
    private float lastDistance;
    private float lastBearing;

    public WaypointPlan(IWaypointProvider provider){
        this.provider = provider;
    }

    @Override
    protected void start(RobotController controller) throws Exception {
        while(!isInterrupted()) {
            Location currentLocation = navSatToLocation(controller.getNavSatFix());
            LaserScan laserScan = controller.getLaserScan();
            Odometry odom = controller.getOdometry();
            Location destination = provider.getDestination();

            if (destination != null) {
                float currentDistance = currentLocation.distanceTo(destination);
                float destinationBearing = (float)Math.toRadians(currentLocation.bearingTo(destination));
                float currentBearing = (float)(2*Math.PI - getRotation(Quaternion.fromQuaternionMessage(odom.getPose().getPose().getOrientation())));

                if(currentDistance < 1){
                    controller.publishVelocity(0,0);
                }
                else{

                }

                lastBearing = currentBearing;
                lastDistance = currentDistance;
            }

            lastLocation = currentLocation;
        }
    }

    private float getRotation(Quaternion quaternion) {
        Vector3 xAxis = Vector3.xAxis();
        Vector3 rotatedAxis = quaternion.rotateAndScaleVector(xAxis);
        rotatedAxis = new Vector3(rotatedAxis.getX(),rotatedAxis.getY(),0);
        rotatedAxis.normalize();
        float angle = (float)Math.acos(xAxis.dotProduct(rotatedAxis));

        if(rotatedAxis.getY() < 0){
            angle = (float)(2*Math.PI - angle);
        }

        return angle;
    }

    private Location navSatToLocation(NavSatFix navSatFix){
        Location location = new Location(navSatFix.getHeader().getFrameId());

        location.setLatitude(navSatFix.getLatitude());
        location.setLongitude(navSatFix.getLongitude());
        location.setAltitude(navSatFix.getAltitude());

        return location;
    }
}
