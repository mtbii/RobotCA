package com.robotca.ControlApp.Core;

import android.location.Location;

import org.ros.rosjava_geometry.Quaternion;
import org.ros.rosjava_geometry.Vector3;

import sensor_msgs.NavSatFix;

/**
 * Created by Michael Brunson on 3/4/16.
 */
public class Utils {
    public static double getHeading(Quaternion quaternion) {
        Vector3 xAxis = Vector3.xAxis();
        Vector3 rotatedAxis = quaternion.rotateAndScaleVector(xAxis);
        rotatedAxis = new Vector3(rotatedAxis.getX(),rotatedAxis.getY(),0);
        rotatedAxis = rotatedAxis.normalize();
        double angle = (float)Math.atan2(rotatedAxis.getY(), rotatedAxis.getX());

        return angle;
    }

    public static Location navSatToLocation(NavSatFix navSatFix){
        Location location = new Location(navSatFix.getHeader().getFrameId());

        location.setLatitude(navSatFix.getLatitude());
        location.setLongitude(navSatFix.getLongitude());
        location.setAltitude(navSatFix.getAltitude());

        return location;
    }

    public static Vector3 rotateVector(Vector3 originalVector, double radians){
        Vector3 offset = new Vector3(originalVector.getX()*Math.cos(radians)-originalVector.getY()*Math.sin(radians),
                originalVector.getX()*Math.sin(radians)+originalVector.getY()*Math.cos(radians), 0);
        return offset;
    }
}
