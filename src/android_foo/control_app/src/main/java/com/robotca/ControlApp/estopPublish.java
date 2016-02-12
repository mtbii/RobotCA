package com.robotca.ControlApp;

/**
 * Created by lightyz on 2/12/16.
 */

import android.util.Log;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.Timer;
import java.util.TimerTask;

import nav_msgs.Odometry;

public class estopPublish {

    public Publisher<geometry_msgs.Twist> publisher;
    private Timer publisherTimer;

    private volatile boolean publishVelocity;
    private String topicName;


    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void onStart(ConnectedNode connectedNode)
    {
        publisher = connectedNode.newPublisher(topicName, geometry_msgs.Twist._TYPE);
        currentVelocityCommand = publisher.newMessage();

        publisherTimer = new Timer();
        publisherTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (publishVelocity) {
                    publisher.publish(currentVelocityCommand);
                }
            }
        }, 0, 80);
    }


    private geometry_msgs.Twist currentVelocityCommand;


    public void publishVelocity(double linearVelocityX, double linearVelocityY,
                                 double angularVelocityZ) {
        if (currentVelocityCommand != null) {
            currentVelocityCommand.getLinear().setX(linearVelocityX);
            currentVelocityCommand.getLinear().setY(-linearVelocityY);
            currentVelocityCommand.getLinear().setZ(0);
            currentVelocityCommand.getAngular().setX(0);
            currentVelocityCommand.getAngular().setY(0);
            currentVelocityCommand.getAngular().setZ(-angularVelocityZ);
        } else {
            Log.w("Emergency Stop", "currentVelocityCommand is null");
        }
    }

    public void onShutdownComplete(Node node) {
        publisherTimer.cancel();
        publisherTimer.purge();
    }
}
