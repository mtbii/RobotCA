package com.robotca.ControlApp.Core.Plans;

import com.robotca.ControlApp.Core.RobotController;

import geometry_msgs.Twist;
import sensor_msgs.LaserScan;
import sensor_msgs.NavSatFix;

/**
 * Created by Michael Brunson on 2/13/16.
 */
public abstract class RobotPlan {
    private Thread thread;

    public boolean isRunning() {
        return thread.isAlive();
    }

    public boolean isInterrupted() {
        return thread.isInterrupted();
    }

    public void stop() {
        if (thread != null) {
            try {
                thread.interrupt();
                thread.join();
            } catch (Exception ignored) {
            }
        }
    }

    public void run(final RobotController controller) {
        stop();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    start(controller);
                } catch (Exception e) {
                }
            }
        });

        thread.start();
    }

    protected abstract void start(RobotController controller) throws Exception;
}
