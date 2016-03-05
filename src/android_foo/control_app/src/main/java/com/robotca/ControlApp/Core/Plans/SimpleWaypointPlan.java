package com.robotca.ControlApp.Core.Plans;

import android.util.Log;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.Core.Utils;
import com.robotca.ControlApp.Fragments.HUDFragment;

import org.ros.rosjava_geometry.Vector3;

/**
 * Rudimentary waypoint plan for testing. No collision detection, just moves towards the next waypoint.
 *
 * Created by Nathaniel Stone on 3/4/16.
 */
public class SimpleWaypointPlan extends RobotPlan {

    private final ControlApp controlApp;

    private static final String TAG = "SimpleWaypointPlan";

    /**
     * Creates a SimpleWaypointPlan for the specified ControlApp.
     * @param controlApp The ControlApp
     */
    public SimpleWaypointPlan (ControlApp controlApp) {
        this.controlApp = controlApp;
    }

    @Override
    protected void start(RobotController controller) throws Exception {

        Log.d(TAG, "Started");

        Vector3 next;
        double dir, dist;

        while (!isInterrupted()) {

            Log.d(TAG, "begin loop");

            while (controlApp.getDestination() == null)
                waitFor(1000L);

            next = controlApp.getDestination();
            Log.d(TAG, "Found next point: (" + next.getX() + ", " + next.getY() + ")");

            // First, check angle
            dir = Utils.pointDirection(HUDFragment.getX(), HUDFragment.getY(), next.getX(), next.getY());
            dir = Utils.angleDifference(HUDFragment.getHeading(), dir);

            Log.d(TAG, "Direction to point: " + (dir * 180 / Math.PI));

            while (Math.abs(dir) * 180.0 / Math.PI > 5.0) {

                controller.publishVelocity(0.0, 0.0, dir / 2.0);
                waitFor((long) (Math.abs(dir) * 180.0 / 10.0));

                dir = Utils.pointDirection(HUDFragment.getX(), HUDFragment.getY(), next.getX(), next.getY());
                dir = Utils.angleDifference(HUDFragment.getHeading(), dir);
            }

            controller.publishVelocity(0.0, 0.0, 0.0);

            Log.d(TAG, "facing point");

            // check distance
            dist = Utils.distance(HUDFragment.getX(), HUDFragment.getY(), next.getX(), next.getY());

            controller.publishVelocity(0.75, 0.0, 0.0);
            waitFor((long) (dist * 100.0));
            controller.publishVelocity(0.0, 0.0, 0.0);

            dist = Utils.distance(HUDFragment.getX(), HUDFragment.getY(), next.getX(), next.getY());
            if (dist < 1.0) // TODO magic number
                controlApp.pollDestination();

            waitFor(100L);
        }

    }
}
