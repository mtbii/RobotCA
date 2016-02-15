package com.robotca.ControlApp.Core;

import android.content.Context;
import android.preference.PreferenceManager;

import com.robotca.ControlApp.Core.Plans.RobotPlan;
import com.robotca.ControlApp.R;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import geometry_msgs.Twist;
import nav_msgs.Odometry;
import sensor_msgs.LaserScan;
import sensor_msgs.NavSatFix;

/**
 * Created by Michael Brunson on 2/13/16.
 */
public class RobotController implements NodeMain {

    private final Context context;

    private Publisher<Twist> movePublisher;
    private Twist currentMove;

    private Subscriber<NavSatFix> navSatFixSubscriber;
    private NavSatFix navSatFix;
    private Object navSatFixMutex;

    private Subscriber<LaserScan> laserScanSubscriber;
    private LaserScan laserScan;
    private Object laserScanMutex;

    private Subscriber<Odometry> odometrySubscriber;
    private Odometry odometry;
    private Object odometryMutex;

    //    private NodeMainExecutor nodeMainExecutor;
//    private NodeConfiguration nodeConfiguration;
    private RobotPlan motionPlan;
    private ConnectedNode connectedNode;

    public RobotController(Context context) {
        this.context = context;
        navSatFixMutex = new Object();
        laserScanMutex = new Object();
    }

    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        nodeMainExecutor.execute(this, nodeConfiguration.setNodeName("android/robot_controller"));
    }

    public void runPlan(RobotPlan plan) {
        stop();
        motionPlan = plan;
        motionPlan.run(this);
    }

    public void stop() {
        if (motionPlan != null) {
            motionPlan.stop();
            motionPlan = null;
        }

        publishVelocity(0, 0);
    }

    public void publishVelocity(double forwardVelocity, double angularVelocity) {
        if (currentMove != null) {
            currentMove.getLinear().setX(forwardVelocity);
            currentMove.getLinear().setY(0);
            currentMove.getLinear().setZ(0);
            currentMove.getAngular().setX(0);
            currentMove.getAngular().setY(0);
            currentMove.getAngular().setZ(-angularVelocity);
            movePublisher.publish(currentMove);
        }
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android/robot_controller");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        this.connectedNode = connectedNode;
        update();
    }

    public void update() {
        if(this.connectedNode != null) {
            if (movePublisher != null) {
                movePublisher.shutdown();
            }

            if (navSatFixSubscriber != null) {
                navSatFixSubscriber.shutdown();
            }

            if (laserScanSubscriber != null) {
                laserScanSubscriber.shutdown();
            }

            String moveTopic = PreferenceManager.getDefaultSharedPreferences(context).getString("edittext_joystick_topic", context.getString(R.string.joy_topic));
            String navSatTopic = PreferenceManager.getDefaultSharedPreferences(context).getString("edittext_camera_topic", context.getString(R.string.camera_topic));
            String laserScanTopic = PreferenceManager.getDefaultSharedPreferences(context).getString("edittext_laser_scan_topic", context.getString(R.string.laser_scan_topic));
            String odometryTopic = PreferenceManager.getDefaultSharedPreferences(context).getString("edittext_odometry_topic", context.getString(R.string.odometry_topic));

            movePublisher = connectedNode.newPublisher(moveTopic, Twist._TYPE);
            currentMove = movePublisher.newMessage();

            navSatFixSubscriber = connectedNode.newSubscriber(navSatTopic, NavSatFix._TYPE);
            navSatFixSubscriber.addMessageListener(new MessageListener<NavSatFix>() {

                @Override
                public void onNewMessage(NavSatFix navSatFix) {
                    setNavSatFix(navSatFix);
                }
            });

            laserScanSubscriber = connectedNode.newSubscriber(laserScanTopic, LaserScan._TYPE);
            laserScanSubscriber.addMessageListener(new MessageListener<LaserScan>() {
                @Override
                public void onNewMessage(LaserScan laserScan) {
                    setLaserScan(laserScan);
                }
            });

            odometrySubscriber = connectedNode.newSubscriber(odometryTopic, Odometry._TYPE);
            odometrySubscriber.addMessageListener(new MessageListener<Odometry>() {
                @Override
                public void onNewMessage(Odometry odometry) {
                    setOdometry(odometry);
                }
            });
        }
    }

    @Override
    public void onShutdown(Node node) {
    }

    @Override
    public void onShutdownComplete(Node node) {
        this.connectedNode = null;
    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }

    public LaserScan getLaserScan() {
        synchronized (laserScanMutex) {
            return laserScan;
        }
    }

    protected void setLaserScan(LaserScan laserScan) {
        synchronized (laserScanMutex) {
            this.laserScan = laserScan;
        }
    }

    public NavSatFix getNavSatFix() {
        synchronized (navSatFixMutex) {
            return navSatFix;
        }
    }

    protected void setNavSatFix(NavSatFix navSatFix) {
        synchronized (navSatFixMutex) {
            this.navSatFix = navSatFix;
        }
    }

    public Odometry getOdometry() {
        synchronized (odometryMutex) {
            return odometry;
        }
    }

    protected void setOdometry(Odometry odometry) {
        synchronized (odometryMutex) {
            this.odometry = odometry;
        }
    }
}
