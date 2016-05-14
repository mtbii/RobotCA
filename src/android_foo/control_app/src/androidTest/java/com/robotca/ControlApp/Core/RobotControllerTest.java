package com.robotca.ControlApp.Core;

import android.support.test.rule.ActivityTestRule;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.Plans.RobotPlan;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ros.internal.message.RawMessage;
import org.ros.message.MessageListener;

import geometry_msgs.PoseWithCovariance;
import geometry_msgs.TwistWithCovariance;
import nav_msgs.Odometry;
import sensor_msgs.LaserScan;
import sensor_msgs.NavSatFix;
import sensor_msgs.NavSatStatus;
import std_msgs.Header;

import static org.junit.Assert.*;

/**
 * Test class for RobotController.
 *
 * Created by Nathaniel on 4/2/16.
 */
public class RobotControllerTest {

    static
    {
        ControlApp.ROBOT_INFO = new RobotInfo(null, "HUDTestRobotInfo", "HUDMasterURI", null, null, null, null, null, null, false, false, false, false);
    }

    @Rule
    public ActivityTestRule<ControlApp> controlAppRule = new ActivityTestRule<>(ControlApp.class);

    // Robot Controller to test
    private RobotController robotController;

    // Things with which to test
    private MessageListener<Odometry> odometryListener;
    private MessageListener<NavSatFix> navSatFixListener;
    private MessageListener<LaserScan> laserScanListener;

    private boolean laserScanSet, navSatSet, odometrySet;

    private RobotPlan robotPlan;

    @Before
    public void setUp() throws Exception {

        // Create the RobotController
        robotController = new RobotController(controlAppRule.getActivity());

        // Create the listeners
        odometryListener = new MessageListener<Odometry>() {
            @Override
            public void onNewMessage(Odometry odometry) {
                odometrySet = true;
            }
        };

        navSatFixListener = new MessageListener<NavSatFix>() {
            @Override
            public void onNewMessage(NavSatFix navSatFix) {
                navSatSet = true;
            }
        };

        laserScanListener = new MessageListener<LaserScan>() {
            @Override
            public void onNewMessage(LaserScan laserScan) {
                laserScanSet = true;
            }
        };

        robotPlan = ControlMode.getRobotPlan(controlAppRule.getActivity(), ControlMode.SimpleWaypoint);
    }

    @Test
    public void testAddOdometryListener() throws Exception {
        assertEquals(true, robotController.addOdometryListener(odometryListener));
    }

    @Test
    public void testAddNavSatFixListener() throws Exception {
        assertEquals(true, robotController.addNavSatFixListener(navSatFixListener));
    }

    @Test
    public void testAddLaserScanListener() throws Exception {
        assertEquals(true, robotController.addLaserScanListener(laserScanListener));
    }

    @Test
    public void testRemoveLaserScanListener() throws Exception {
        assertEquals(true, robotController.addLaserScanListener(laserScanListener));
        assertEquals(true, robotController.removeLaserScanListener(laserScanListener));
    }

    @Test
    public void testRunPlan() throws Exception {
        robotController.runPlan(robotPlan);

        Thread.sleep(100L);

        assertEquals(true, robotPlan.isRunning());
    }

    @Test
    public void testResumePlan() throws Exception {
        // Make sure resuming no plan works
        robotController.runPlan(null);
        assertEquals(false, robotController.resumePlan());

        // Make sure resuming a paused plan works
        robotController.runPlan(robotPlan);
        robotController.stop();
        assertEquals(true, robotController.resumePlan());
    }

    @Test
    public void testGetMotionPlan() throws Exception {
        robotController.runPlan(null);
        assertEquals(null, robotController.getMotionPlan());

        robotController.runPlan(robotPlan);
        assertEquals(robotPlan, robotController.getMotionPlan());
    }

    @Test
    public void testStop() throws Exception {

        robotController.runPlan(robotPlan);

        assertEquals(true, robotController.stop());

        Thread.sleep(100L);
        assertEquals(false, robotPlan.isRunning());
    }

    @Test
    public void testSetLaserScan() throws Exception {
        testAddLaserScanListener();
        robotController.setLaserScan(new LaserScan() {
            @Override
            public Header getHeader() {
                return null;
            }

            @Override
            public void setHeader(Header header) {

            }

            @Override
            public float getAngleMin() {
                return 0;
            }

            @Override
            public void setAngleMin(float v) {

            }

            @Override
            public float getAngleMax() {
                return 0;
            }

            @Override
            public void setAngleMax(float v) {

            }

            @Override
            public float getAngleIncrement() {
                return 0;
            }

            @Override
            public void setAngleIncrement(float v) {

            }

            @Override
            public float getTimeIncrement() {
                return 0;
            }

            @Override
            public void setTimeIncrement(float v) {

            }

            @Override
            public float getScanTime() {
                return 0;
            }

            @Override
            public void setScanTime(float v) {

            }

            @Override
            public float getRangeMin() {
                return 0;
            }

            @Override
            public void setRangeMin(float v) {

            }

            @Override
            public float getRangeMax() {
                return 0;
            }

            @Override
            public void setRangeMax(float v) {

            }

            @Override
            public float[] getRanges() {
                return new float[0];
            }

            @Override
            public void setRanges(float[] floats) {

            }

            @Override
            public float[] getIntensities() {
                return new float[0];
            }

            @Override
            public void setIntensities(float[] floats) {

            }

            @Override
            public RawMessage toRawMessage() {
                return null;
            }
        });

        assertEquals(true, laserScanSet);
    }

    @Test
    public void testSetNavSatFix() throws Exception {
        testAddNavSatFixListener();
        robotController.setNavSatFix(new NavSatFix() {
            @Override
            public Header getHeader() {
                return null;
            }

            @Override
            public void setHeader(Header header) {

            }

            @Override
            public NavSatStatus getStatus() {
                return null;
            }

            @Override
            public void setStatus(NavSatStatus navSatStatus) {

            }

            @Override
            public double getLatitude() {
                return 0;
            }

            @Override
            public void setLatitude(double v) {

            }

            @Override
            public double getLongitude() {
                return 0;
            }

            @Override
            public void setLongitude(double v) {

            }

            @Override
            public double getAltitude() {
                return 0;
            }

            @Override
            public void setAltitude(double v) {

            }

            @Override
            public double[] getPositionCovariance() {
                return new double[0];
            }

            @Override
            public void setPositionCovariance(double[] doubles) {

            }

            @Override
            public byte getPositionCovarianceType() {
                return 0;
            }

            @Override
            public void setPositionCovarianceType(byte b) {

            }

            @Override
            public RawMessage toRawMessage() {
                return null;
            }
        });

        assertEquals(true, navSatSet);
    }

    @Test
    public void testSetOdometry() throws Exception {
        testAddOdometryListener();
        try {
            robotController.setOdometry(new Odometry() {
                @Override
                public Header getHeader() {
                    return null;
                }

                @Override
                public void setHeader(Header header) {

                }

                @Override
                public String getChildFrameId() {
                    return null;
                }

                @Override
                public void setChildFrameId(String s) {

                }

                @Override
                public PoseWithCovariance getPose() {
                    return null;
                }

                @Override
                public void setPose(PoseWithCovariance poseWithCovariance) {

                }

                @Override
                public TwistWithCovariance getTwist() {
                    return null;
                }

                @Override
                public void setTwist(TwistWithCovariance twistWithCovariance) {

                }

                @Override
                public RawMessage toRawMessage() {
                    return null;
                }
            });
        }
        catch (NullPointerException ignore) {}

        assertEquals(true, odometrySet);
    }
}
