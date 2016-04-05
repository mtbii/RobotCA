package com.robotca.ControlApp.Core.Plans;

import com.robotca.ControlApp.Core.ControlMode;
import com.robotca.ControlApp.Core.RobotController;

import java.util.Random;

import sensor_msgs.LaserScan;

/**
 * Simple bump and turn motion plan.
 *
 * Created by Michael Brunson on 2/13/16.
 */
public class RandomWalkPlan extends RobotPlan {

    private final Random random;
    private float minRange;

    /**
     * Creates a RandomWalkPlan with the specified minimum range.
     * @param minRange The minimum range
     */
    public RandomWalkPlan(float minRange) {
        this.minRange = Math.max(minRange, 0.2f);
        random = new Random();
    }

    /**
     * @return The ControlMode for this RobotPlan
     */
    @Override
    public ControlMode getControlMode() {
        return ControlMode.RandomWalk;
    }

    @Override
    public void start(final RobotController controller) throws Exception {
        // Laser scan data
        float[] ranges;
        // Temporary variables
        float shortestDistance, angle, angleDelta, angleIncrement;

        while(!isInterrupted()) {
            LaserScan laserScan = controller.getLaserScan();

            ranges = laserScan.getRanges();
            shortestDistance = ranges[ranges.length / 2];
            angle = laserScan.getAngleMin();
            angleDelta = (float) Math.toRadians(30);
            angleIncrement = laserScan.getAngleIncrement();

            // Find the shortest range
            for (int i = 0; i < laserScan.getRanges().length; i++) {
                if (ranges[i] < shortestDistance && angle > -angleDelta && angle < angleDelta) {
                    shortestDistance = ranges[i];
                }

                angle += angleIncrement;
            }

            // If a wall is close, stop, turn a random amount, and continue moving
            if (shortestDistance < minRange) {
                controller.publishVelocity(0, 0, 0);
                waitFor(1000);

                long delay = (long) (2000 * (1 + random.nextFloat()));
                controller.publishVelocity(0, 0, .75);
                waitFor(delay);
                controller.publishVelocity(0, 0,  0);
                waitFor(1000);
            } else {
                controller.publishVelocity(.75, 0, 0);
            }
        }
    }
}
