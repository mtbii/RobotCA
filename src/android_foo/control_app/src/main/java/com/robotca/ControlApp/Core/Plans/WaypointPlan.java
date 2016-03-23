package com.robotca.ControlApp.Core.Plans;

import android.util.Log;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.Core.Utils;
import com.robotca.ControlApp.Fragments.HUDFragment;

import org.ros.rosjava_geometry.Vector3;

import sensor_msgs.LaserScan;

/**
 * Created by Mike on 2/13/2016.
 */
public class WaypointPlan extends RobotPlan {

    private final static double GAMMA = 50;
    private final static double ALPHA = 5;
    private final static double BETA = 5;
    private final static double EPSILON = 0.1;
    private final static double D_SAFE = .75;
    private final static double KAPPA = 0.45;
    private final static double FORWARD_SPEED_MPS = .15;
    private static final double MINIMUM_DISTANCE = 1.0;

    private ControlApp controlApp;

    private Vector3 lastPosition;
    private double lastHeading;

    private Vector3 currentPosition;
    private double currentHeading;
    private Vector3 goalPosition;

    private double angularVelocity;
    private double lastAngularVelocity;
    private double linearVelocity;
    private boolean atGoal;
    private Vector3 randForce;

    private double randScale;
    private int stuckCount;

    public WaypointPlan(ControlApp controlApp) {
        this.controlApp = controlApp;
    }

    @Override
    protected void start(RobotController controller) throws Exception {

        while (!isInterrupted()) {
            LaserScan scan = controller.getLaserScan();

            while (controlApp.getDestination() == null) {
                controller.publishVelocity(0, 0, 0);
                waitFor(1000L);
            }

            currentPosition = new Vector3(RobotController.getX(), RobotController.getY(), 0);
            currentHeading = RobotController.getHeading();
            goalPosition = controlApp.getDestination();

            if (goalPosition != null) {
                Vector3 netForce = calculateForces(scan);
                applyForce(controller, netForce);

                double dist = Utils.distance(RobotController.getX(), RobotController.getY(),
                        goalPosition.getX(), goalPosition.getY());

                if (dist < MINIMUM_DISTANCE)
                    controlApp.pollDestination();
            } else {
                controller.publishVelocity(0, 0, 0);
            }

            lastPosition = currentPosition;
            lastHeading = currentHeading;

            waitFor(100L);
        }
    }

    private void applyForce(RobotController controller, Vector3 netForce) throws InterruptedException {
        double forceAngle = 0;
        if (netForce.getY() != 0 || netForce.getX() != 0)
            forceAngle = Math.atan2(netForce.getY(), netForce.getX());

        if (forceAngle < 0) {
            forceAngle += 2 * Math.PI;
        }

        if (currentHeading < 0) {
            currentHeading += 2 * Math.PI;
        }

        Log.d("ControlApp", String.format("Force Angle: %f", Math.toDegrees(forceAngle)));
        Log.d("ControlApp", String.format("Heading:     %f", Math.toDegrees(currentHeading)));

        //compute angular vel
        double angle1 = forceAngle - currentHeading;
        double angle2 = 2 * Math.PI - Math.abs(angle1);
        double angle = 0;

        if (Math.abs(angle1) > Math.abs(angle2)) {
            angle = -angle2;
        } else {
            angle = angle1;
        }

        Log.d("ControlApp", String.format("Turn Angle:  %f", angle));

        angularVelocity = -KAPPA * angle;

        // if(angVel > ROTATE_SPEED_RADPS){
        //   angVel = ROTATE_SPEED_RADPS;
        // }
        // else if(angVel < -ROTATE_SPEED_RADPS){
        //   angVel = -ROTATE_SPEED_RADPS;
        // }

        //compute linear vel
        double linVel = (netForce.getMagnitude() * Math.cos(angle1));
        if (linVel < 0) {
            linearVelocity = 0;
        } else
            linearVelocity = linVel;

        if (linearVelocity > FORWARD_SPEED_MPS)
            linearVelocity = FORWARD_SPEED_MPS;

//        if(Math.abs(Math.abs(lastAngularVelocity) - Math.abs(angularVelocity)) < .01 * KAPPA && !atGoal){
//            angularVelocity = 0;
//            linearVelocity = FORWARD_SPEED_MPS;
//        }
//        else{
//            lastAngularVelocity = angularVelocity;
//        }

//        if(Math.abs(Math.abs(lastAngularVelocity) - Math.abs(angularVelocity)) < .01 * KAPPA && !atGoal){
//            angularVelocity = 0;
//            linearVelocity = FORWARD_SPEED_MPS;
//        }
//        else{
//            lastAngularVelocity = angularVelocity;
//        }

        Log.d("ControlApp", String.format("Net Force: (%f, %f)", netForce.getX(), netForce.getY()));
        Log.d("ControlApp", String.format("Velocity: (%f, %f)", linearVelocity, angularVelocity));
        Log.d("ControlApp", String.format("Position: (%f, %f); Goal: (%f, %f)", currentPosition.getX(), currentPosition.getY(), goalPosition.getX(), goalPosition.getY()));
        controller.publishVelocity(this.linearVelocity, 0, angularVelocity);

        //Wait a little while before stopping the rotation
        //This prevents unstable behavior when needing to rotate 180 degrees
//        if (linVel < 0) {
//            waitFor((long)(500.0 / KAPPA));
//        }
    }

    private Vector3 calculateForces(LaserScan laserScan) {
        Vector3 netForce = new Vector3(0, 0, 0);

        Vector3 attractiveForce = goalPosition.subtract(currentPosition);
        attractiveForce = attractiveForce.scale(GAMMA * attractiveForce.getMagnitude()); // f_a = gamma * ||x_g - x_r||^2 = |x_g - x_r|| * gamma * |||x_g - x_r||
        netForce = netForce.add(attractiveForce);

        Log.d("ControlApp", String.format("Attractive Force: (%f, %f)", attractiveForce.getX(), attractiveForce.getY()));

        for (int i = 0; i < laserScan.getRanges().length; i++) {
            double angleOffset = laserScan.getAngleMin() + laserScan.getAngleIncrement() * i;
            double angle = angleOffset + currentHeading; //correct for robot heading
            double distance = laserScan.getRanges()[i];
            double scalar = 0;

            //scale for distance
            if (D_SAFE + EPSILON < distance && distance < BETA) {
                double diff = distance - D_SAFE;
                scalar = ALPHA / (diff * diff);
            } else if (distance < D_SAFE + EPSILON) {
                scalar = ALPHA / (EPSILON * EPSILON);
            }

            //force points opposite to repel
            Vector3 force = new Vector3(Math.cos(angle), Math.sin(angle), 0);
            force = force.scale(-scalar);
            netForce = netForce.add(force);
        }

//        if (Math.abs(currentPosition.getX() - goalPosition.getX()) < 1 && Math.abs(currentPosition.getY() - goalPosition.getY()) < 1) {
//            randForce = new Vector3(Math.random(), Math.random(), 0);
//            netForce = new Vector3(0, 0, 0);
//            randScale = 1.0;
//            stuckCount = 0;
//            atGoal = true;
//        } else if (Math.abs(lastPosition.getX() - currentPosition.getX()) < 0.0001 && Math.abs(lastPosition.getY() - currentPosition.getY()) < 0.0001) {
//            if (stuckCount > 30) {
//                randForce = randForce.scale(randScale);
//                netForce = netForce.add(randForce);
//                randScale *= 2;
//            } else
//                stuckCount++;
//        } else {
//            randForce = new Vector3(Math.random(), Math.random(), 0);
//            randScale = 1.0;
//            stuckCount = 0;
//        }

        return netForce;
    }
}