package com.robotca.ControlApp.Core.Plans;

import android.util.Log;

import com.robotca.ControlApp.Core.ControlMode;
import com.robotca.ControlApp.Core.RobotController;

import geometry_msgs.Twist;
import sensor_msgs.LaserScan;
import sensor_msgs.NavSatFix;

/**
 *
 *
 * Created by Michael Brunson on 2/13/16.
 */
public abstract class RobotPlan {

    // The thread this RobotPlan runs on
    private Thread thread;

    // If it makes sense for this RobotPlan to be resumed if stopped
    private boolean resumable;

    // Log Tag
    private static final String TAG = "RobotPlan";

    /**
     * Default Constructor.
     */
    public RobotPlan() {
        setResumable(true);
    }

    /**
     * @return If this RobotPlan is running
     */
    public boolean isRunning() {
        return thread.isAlive();
    }

    /**
     * @return If this RobotPlan has been interrupted
     */
    public boolean isInterrupted() {
        return thread.isInterrupted();
    }

    /**
     * @return True if this RobotPlan can be resumed, false otherwise
     */
    public boolean isResumable() {
        return resumable;
    }

    /**
     * Sets whether this RobotPlan can be resumed if stopped.
     * @param resumable True if it can be resumed, false otherwise
     */
    protected void setResumable(boolean resumable) {
        this.resumable = resumable;
    }

    /**
     * Stops this RobotPlan, interrupting it.
     */
    public void stop() {
        if (thread != null) {

            Log.d(TAG, "Stopping plan");

            try {
                thread.interrupt();
                thread.join(1000L);
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        } else {
            Log.d(TAG, "No Thread to Stop");
        }
    }

    /**
     * @return The ControlMode for this RobotPlan
     */
    public abstract ControlMode getControlMode();

    /**
     * Runs this RobotPlan.
     * @param controller The parent RobotController
     */
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
     * Pauses this RobotPlan for the specified amount of time.
     *
     * @param milliseconds The time, in milliseconds
     * @throws InterruptedException If the RobotPlan gets interrupted
     */
    protected void waitFor(long milliseconds) throws InterruptedException {
        try {
            long currentTime = System.currentTimeMillis();
            while (currentTime + milliseconds > System.currentTimeMillis() && !isInterrupted())
                Thread.sleep(milliseconds / 3);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    /**
     * Called when the RobotPlan is started.
     * @param controller The RobotController for controlling the Robot
     * @throws Exception If an exception occurs
     */
    protected abstract void start(RobotController controller) throws Exception;
}
