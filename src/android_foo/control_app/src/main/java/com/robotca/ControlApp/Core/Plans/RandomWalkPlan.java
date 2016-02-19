package com.robotca.ControlApp.Core.Plans;

import com.robotca.ControlApp.Core.RobotController;

import java.util.Random;
import java.util.Timer;

import sensor_msgs.LaserScan;

/**
 * Created by Michael Brunson on 2/13/16.
 */
public class RandomWalkPlan extends RobotPlan {

    private final Random random;
    private float minRange;

    public RandomWalkPlan(float minRange) {
        this.minRange = Math.max(minRange, 0.2f);
        random = new Random();
    }

    @Override
    public void start(final RobotController controller) throws Exception {
        while(!isInterrupted()) {
            LaserScan laserScan = controller.getLaserScan();

            float[] ranges = laserScan.getRanges();
            float shortestDistance = ranges[ranges.length / 2];
            float shortestDistanceAngle = 0;
            float angle = laserScan.getAngleMin();
            float angleDelta = (float) Math.toRadians(30);
            float angleIncrement = laserScan.getAngleIncrement();

            for (int i = 0; i < laserScan.getRanges().length; i++) {
                if (ranges[i] < shortestDistance && angle > -angleDelta && angle < angleDelta) {
                    shortestDistance = ranges[i];
                    shortestDistanceAngle = angle;
                }

                angle += angleIncrement;
            }

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
