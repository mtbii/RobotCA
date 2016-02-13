package com.robotca.ControlApp.Core.Plans;

import com.robotca.ControlApp.Core.RobotController;

import java.util.Random;
import java.util.Timer;

import sensor_msgs.LaserScan;

/**
 * Created by Michael Brunson on 2/13/16.
 */
public class RandomWalkPlan implements IRobotPlan {

    private final Random random;

    public RandomWalkPlan() {
        random = new Random();
    }

    @Override
    public void run(final RobotController controller) throws Exception {
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

        if (shortestDistance < 20 * laserScan.getRangeMin()) {
            controller.publishVelocity(0, 0);
            Thread.sleep(1000, 0);
            Timer timer = new Timer();
            long delay = (long) (3000 * (1 + random.nextFloat()));
            long start = System.currentTimeMillis();
            while(!Thread.interrupted() && System.currentTimeMillis() - start < delay){
                controller.publishVelocity(0, .75);
            }
            controller.publishVelocity(0, 0);
            Thread.sleep(1000, 0);
        } else {
            controller.publishVelocity(.75, 0);
        }
    }
}
