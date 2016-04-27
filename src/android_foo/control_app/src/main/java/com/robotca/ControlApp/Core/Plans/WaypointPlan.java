package com.robotca.ControlApp.Core.Plans;

import android.util.Log;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.ControlMode;
import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.Core.Utils;

import org.ros.rosjava_geometry.Vector3;

import java.util.Random;

import sensor_msgs.LaserScan;

/**
 * Motion plan for following Waypoints using a potential field for obstacle avoidance.
 *
 * Created by Mike on 2/13/2016.
 */
public class WaypointPlan extends RobotPlan {

    // Parameters for the potential field calculation
    private final static double GAMMA = 2;
    private final static double ALPHA = 6;
    private final static double BETA = 1.25;
    private final static double EPSILON = 0.01;
    private final static double D_SAFE = 0.25;
    private final static double KAPPA = 0.4;
    private final static double FORWARD_SPEED_MPS = 0.75;
    private static final double MINIMUM_DISTANCE = 1.0;

    // Reference to the ControlApp
    private ControlApp controlApp;

    private Vector3 lastPosition;

    private Random random = new Random();
    private long stuckTime = -1;
    @SuppressWarnings("FieldCanBeLocal")
    private long randScale;

    private Vector3 randForce;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3 finalRandForce;
    private Vector3 stuckPosition;

    private Vector3 currentPosition;
    private double currentHeading;
    private Vector3 goalPosition;

    @SuppressWarnings("FieldCanBeLocal")
    private double angularVelocity;
    @SuppressWarnings("FieldCanBeLocal")
    private double linearVelocity;

    public WaypointPlan(ControlApp controlApp) {
        this.controlApp = controlApp;
    }

    /**
     * @return The ControlMode for this RobotPlan
     */
    @Override
    public ControlMode getControlMode() {
        return ControlMode.Waypoint;
    }

    /**
     * Starts the WaypointPlan.
     * @param controller The RobotController for controlling the Robot
     * @throws Exception If an error occurs
     */
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
            waitFor(100L);
        }
    }

    /*
     * Calculates the potential field forces from the specified LaserScan. These forces are used to
     * 'repel' the Robot from obstacles, thus guiding it around them.
     */
    private Vector3 calculateForces(LaserScan laserScan) {
        Vector3 netForce = new Vector3(0, 0, 0);

        Vector3 attractiveForce = goalPosition.subtract(currentPosition);
        attractiveForce = attractiveForce.scale(GAMMA * attractiveForce.getMagnitude()); // f_a = gamma * ||x_g - x_r||^2 = |x_g - x_r|| * gamma * |||x_g - x_r||
        netForce = netForce.add(attractiveForce);

        Log.d("ControlApp", String.format("Attr. Force: (%f, %f)", attractiveForce.getX(), attractiveForce.getY()));

        for (int i = 0; i < laserScan.getRanges().length; i++) {
            //Husky laser ranges go from max to min
            double angleOffset = laserScan.getAngleMax() - laserScan.getAngleIncrement() * i;
            double angle = angleOffset + currentHeading; //correct for robot heading
            double distance = laserScan.getRanges()[i];
            double scalar = 0;

            if (distance >= 5*EPSILON && distance != Double.NaN) {
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
        }

        try {
            if (stuckPosition == null) {
                if (currentPosition.subtract(lastPosition).getMagnitude() < FORWARD_SPEED_MPS / 2.0) {
                    stuckTime = System.currentTimeMillis();
                    stuckPosition = currentPosition;

                    double randAngle = random.nextDouble() * 2.0 * Math.PI;
                    randForce = new Vector3(Math.cos(randAngle), Math.sin(randAngle), 0);
                }
            } else {
                if (currentPosition.subtract(stuckPosition).getMagnitude() < FORWARD_SPEED_MPS / 2.0) {
                    long deltaTime = System.currentTimeMillis() - stuckTime;
                    randScale = deltaTime / 1000L;
                    Log.d("ControlApp", String.format("Rand Scale: %d", randScale));

                    finalRandForce = randForce.scale(randScale);
                    netForce = netForce.add(finalRandForce);
                } else {
                    stuckPosition = null;
                }
            }
        } catch(Exception ignore){}

        return netForce;
    }

    /*
     * Applies the specified force to the Robot, adjusting its motion accordingly.
     */
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

        Log.d("ControlApp", String.format("Net Force: (%f, %f)", netForce.getX(), netForce.getY()));
        Log.d("ControlApp", String.format("Force Angle: %f", Math.toDegrees(forceAngle)));
        Log.d("ControlApp", String.format("Heading:     %f", Math.toDegrees(currentHeading)));

        //compute angular vel
        double angle1 = forceAngle - currentHeading;
        double angle2 = 2 * Math.PI - Math.abs(angle1);
        double angle;

        if (Math.abs(angle1) > Math.abs(angle2)) {
            angle = -angle2;
        } else {
            angle = angle1;
        }

        Log.d("ControlApp", String.format("Turn Angle:  %f", angle));

        angularVelocity = -KAPPA * angle;

        //compute linear vel
        double linVel = (netForce.getMagnitude() * Math.cos(angle1));
        if (linVel < 0) {
            linearVelocity = 0;
        } else
            linearVelocity = linVel;

        if (linearVelocity > FORWARD_SPEED_MPS)
            linearVelocity = FORWARD_SPEED_MPS;

        controller.publishVelocity(this.linearVelocity, 0, angularVelocity);
    }
}