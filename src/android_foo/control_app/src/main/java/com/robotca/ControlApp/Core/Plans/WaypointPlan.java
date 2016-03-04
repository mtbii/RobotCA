package com.robotca.ControlApp.Core.Plans;

import android.util.Log;

import com.robotca.ControlApp.Core.IWaypointProvider;
import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.Core.Utils;

import org.ros.rosjava_geometry.Quaternion;
import org.ros.rosjava_geometry.Vector3;

import geometry_msgs.Pose;
import nav_msgs.Odometry;
import sensor_msgs.LaserScan;

/**
 * Created by Mike on 2/13/2016.
 */
public class WaypointPlan extends RobotPlan {

    final static double GAMMA = 3;
    final static double ALPHA = 1;
    final static double BETA = 4;
    final static double EPSILON = 0.01;
    final static double D_SAFE = 0.1;
    final static double KAPPA = 0.1;
    final static double FORWARD_SPEED_MPS = .5;

    private IWaypointProvider provider;

    private Vector3 lastPosition;
    private double lastHeading;
    private Vector3 currentPosition;
    private double currentHeading;
    private Vector3 goalPosition;

    private double angularVelocity;
    private double lastAngularVelocity;
    private double linearVelocity;
    private boolean atGoal;

    public WaypointPlan(IWaypointProvider provider){
        this.provider = provider;
    }

    @Override
    protected void start(RobotController controller) throws Exception {
        while(!isInterrupted()) {
            Pose pose = controller.getPose(); //navSatToLocation(controller.getNavSatFix());
            LaserScan scan = controller.getLaserScan();
            Odometry odom = controller.getOdometry();

            if(pose == null){
                pose = odom.getPose().getPose();
            }

            currentPosition = new Vector3(pose.getPosition().getX(), pose.getPosition().getY(), 0);
            currentHeading = Utils.getHeading(Quaternion.fromQuaternionMessage(pose.getOrientation()));
            goalPosition = provider.getDestination();

            if (goalPosition != null) {
                Vector3 netForce = calculateForces(pose, scan);
                applyForce(controller, netForce);
            }

            lastPosition = currentPosition;
            lastHeading = currentHeading;
        }
    }

    private void applyForce(RobotController controller, Vector3 netForce) {
        double forceAngle = 0;
        if(netForce.getY() != 0 || netForce.getX() != 0)
            forceAngle = Math.atan2(netForce.getY(), netForce.getX());

        if(forceAngle < 0) {
            forceAngle += 2 * Math.PI;
        }

        if(currentHeading < 0){
            currentHeading += 2 * Math.PI;
        }

        //compute angular vel
        double angle1 = forceAngle - currentHeading;
        double angle2 = 2*Math.PI - Math.abs(angle1);
        double angle = 0;

        if(Math.abs(angle1) > Math.abs(angle2)){
            angle = -angle2;
        }
        else{
            angle = angle1;
        }

        angularVelocity = KAPPA * angle;

        // if(angVel > ROTATE_SPEED_RADPS){
        //   angVel = ROTATE_SPEED_RADPS;
        // }
        // else if(angVel < -ROTATE_SPEED_RADPS){
        //   angVel = -ROTATE_SPEED_RADPS;
        // }

        //compute linear vel
        linearVelocity = (netForce.getMagnitude() * Math.cos(angle1));
        if(linearVelocity < 0){
            linearVelocity = 0;
        }

        if(linearVelocity > FORWARD_SPEED_MPS)
            linearVelocity = FORWARD_SPEED_MPS;

//        if(Math.abs(Math.abs(lastAngularVelocity) - Math.abs(angularVelocity)) < .01 * KAPPA && !atGoal){
//            angularVelocity = 0;
//            linearVelocity = FORWARD_SPEED_MPS;
//        }
//        else{
//            lastAngularVelocity = angularVelocity;
//        }

        if(!atGoal){
            //this.linearVelocity = 0;
            Log.d("ControlApp", String.format("netForce: (%f, %f); vel: (%f, %f)", netForce.getX(), netForce.getY(), linearVelocity, angularVelocity));
            Log.d("ControlApp", String.format("position: (%f, %f); goal: (%f, %f)", currentPosition.getX(), currentPosition.getY(), goalPosition.getX(), goalPosition.getY()));
            controller.publishVelocity(this.linearVelocity, 0, angularVelocity);
        }
    }


    private Vector3 calculateForces(Pose pose, LaserScan laserScan){
        Vector3 netForce = new Vector3(0,0,0);

        Vector3 attractiveForce = goalPosition.subtract(currentPosition);
        attractiveForce = attractiveForce.scale(GAMMA * attractiveForce.getMagnitude()); // f_a = gamma * ||x_g - x_r||^2 = |x_g - x_r|| * gamma * |||x_g - x_r||
        netForce = netForce.add(attractiveForce);

        for(int i = 0; i < laserScan.getRanges().length; i++){
            double angleOffset = laserScan.getAngleMin() + laserScan.getAngleIncrement()*i;
            double angle = angleOffset + currentHeading; //correct for robot heading
            double distance = laserScan.getRanges()[i];
            double scalar = 0;

            //scale for distance
            if(D_SAFE + EPSILON < distance && distance < BETA){
                double diff = distance - D_SAFE;
                scalar = ALPHA / (diff*diff);
            }
            else if(distance < D_SAFE + EPSILON) {
                scalar = ALPHA / (EPSILON*EPSILON);
            }

            //force points opposite to repel
            Vector3 force = new Vector3(Math.cos(angle), Math.sin(angle), 0);
            force = force.scale(-scalar);
            netForce = netForce.add(force);
        }

        return netForce;
    }

//    private Vector3 getRPY(Quaternion rot){
//        Vector3 euler = Vector3.zero();
//        double[] mat = new Transform(Vector3.zero(), rot).toMatrix();
//
//        if(Math.abs(mat[2*4]) >= 1){
//
//        }
//
//        return euler;
//    }
}