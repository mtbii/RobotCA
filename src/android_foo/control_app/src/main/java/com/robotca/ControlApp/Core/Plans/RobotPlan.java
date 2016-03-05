package com.robotca.ControlApp.Core.Plans;

import android.util.Log;

import com.robotca.ControlApp.Core.RobotController;

import geometry_msgs.Twist;
import sensor_msgs.LaserScan;
import sensor_msgs.NavSatFix;

/**
 * Created by Michael Brunson on 2/13/16.
 */
public abstract class RobotPlan {
    private Thread thread;

    private static final String TAG = "RobotPlan";

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
                thread.join(1000L);
            } catch (Exception e) {
                Log.e(TAG, "", e);
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
                    Log.e(TAG, "", e);
                }
            }
        });

        thread.start();
    }

    /**
     * Waits for the specified amount of time.
     * @param milliseconds The time, in milliseconds
     * @throws InterruptedException If the thread gets interrupted
     */
    protected void waitFor(long milliseconds) throws InterruptedException{
        try {
            long currentTime = System.currentTimeMillis();
            while (currentTime + milliseconds > System.currentTimeMillis() && !isInterrupted())
                Thread.sleep(milliseconds / 3);
        }
        catch (InterruptedException e) {
            throw e;
        }
        catch(Exception e)
        {
            Log.e(TAG, "", e);
        }
    }

    protected abstract void start(RobotController controller) throws Exception;
}
