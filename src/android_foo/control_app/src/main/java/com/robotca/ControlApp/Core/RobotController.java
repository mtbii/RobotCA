package com.robotca.ControlApp.Core;

import android.content.Context;

import com.robotca.ControlApp.Core.Plans.IRobotPlan;

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

    private NodeMainExecutor nodeMainExecutor;
    private NodeConfiguration nodeConfiguration;
    private String topicName;
    private Thread currentPlanThread;

    public RobotController(Context context) {
        this.context = context;
        navSatFixMutex = new Object();
        laserScanMutex = new Object();
    }

    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        this.nodeMainExecutor = nodeMainExecutor;
        this.nodeConfiguration = nodeConfiguration;

        nodeMainExecutor.execute(this, nodeConfiguration.setNodeName("android/robot_controller"));
    }

    public void runPlan(final IRobotPlan plan) {
        stopPlan();

        currentPlanThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()){
                    try {
                        plan.run(RobotController.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        currentPlanThread.start();
    }

    public void stopPlan(){
        if (currentPlanThread != null) {
            currentPlanThread.interrupt();
            currentPlanThread = null;
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
        movePublisher = connectedNode.newPublisher(topicName, Twist._TYPE);
        currentMove = movePublisher.newMessage();

        navSatFixSubscriber = connectedNode.newSubscriber("/navsat/fix", NavSatFix._TYPE);
        navSatFixSubscriber.addMessageListener(new MessageListener<NavSatFix>() {

            @Override
            public void onNewMessage(NavSatFix navSatFix) {
               setNavSatFix(navSatFix);
            }
        });

        laserScanSubscriber = connectedNode.newSubscriber("/scan", LaserScan._TYPE);
        laserScanSubscriber.addMessageListener(new MessageListener<LaserScan>() {
            @Override
            public void onNewMessage(LaserScan laserScan) {
                setLaserScan(laserScan);
            }
        });
    }

    @Override
    public void onShutdown(Node node) {
    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }

    public String getTopicName() {
        return this.topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public LaserScan getLaserScan() {
        synchronized (laserScanMutex) {
            return laserScan;
        }
    }

    public void setLaserScan(LaserScan laserScan) {
        synchronized (laserScanMutex) {
            this.laserScan = laserScan;
        }
    }

    public NavSatFix getNavSatFix() {
        synchronized (navSatFixMutex) {
            return navSatFix;
        }
    }

    public void setNavSatFix(NavSatFix navSatFix) {
        synchronized (navSatFixMutex) {
            this.navSatFix = navSatFix;
        }
    }
}
