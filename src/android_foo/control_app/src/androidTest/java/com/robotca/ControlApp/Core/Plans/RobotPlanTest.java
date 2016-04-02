package com.robotca.ControlApp.Core.Plans;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.robotca.ControlApp.Core.ControlMode;
import com.robotca.ControlApp.Core.RobotController;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;

/**
 * Test for RobotPlan.
 *
 * Created by Nathaniel on 4/2/16.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RobotPlanTest {

    // Log tag String
    private static final String TAG = "*****RobotPlanTest*****";

    private RobotPlan robotPlan;

    private static volatile boolean started, running, interrupted;

    @Before
    public void setup() {

        robotPlan = new RobotPlan() {
            @Override
            public ControlMode getControlMode() {
                return null;
            }

            @Override
            protected void start(RobotController controller) throws Exception {
                started = true;
                running = true;
                interrupted = false;

                Log.d(TAG, "Started");
                //noinspection StatementWithEmptyBody
                while (!isInterrupted());
                Log.d(TAG, "Finished");

                interrupted = true;
                running = false;
            }
        };
    }

    @Test
    public void B_testIsRunning() throws Exception {
        // Make sure it's running
        assertEquals(true, running);
    }

    @Test
    public void D_testIsInterrupted() throws Exception {
        // Verify it's interrupted
        assertEquals(true, interrupted);
    }

    @Test
    public void C_testStop() throws Exception {
        robotPlan.run(null);

        // Wait for it to start
        Thread.sleep(100L);

        // Stop it
        Log.d(TAG, "STOPPING");
        robotPlan.stop();

        // Give it time to stop
        Thread.sleep(100L);

        // Make sure it has stopped
        assertEquals(false, running);
    }

    @Test
    public void A_testRun() throws Exception {
        robotPlan.run(null);

        // Wait for it to start
        Thread.sleep(100L);

        // Make sure it started
        assertEquals(true, started);
    }
}