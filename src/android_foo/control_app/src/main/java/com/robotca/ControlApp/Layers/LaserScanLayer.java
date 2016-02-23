package com.robotca.ControlApp.Layers;

/**
 * Improved version of the ros laser scan layer.
 * Instead of using a preset stride to limit the number of points drawn, points are drawn when their
 * distance from the last drawn point exceeds some value.
 * Created by Nathaniel on 2/12/16.
 */
/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.common.base.Preconditions;

import org.ros.android.view.visualization.Color;
import org.ros.android.view.visualization.Vertices;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.SubscriberLayer;
import org.ros.android.view.visualization.layer.TfLayer;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import sensor_msgs.LaserScan;

public class LaserScanLayer extends SubscriberLayer<LaserScan> implements TfLayer {

    private static final Color FREE_SPACE_COLOR = Color.fromHexAndAlpha("377dfa", 0.1f);
    private static final Color OCCUPIED_SPACE_COLOR = Color.fromHexAndAlpha("377dfa", 0.3f);
    private static final float LASER_SCAN_POINT_SIZE = 10.f;
//    private static final int LASER_SCAN_STRIDE = 15;

    // Only adds laser scan points if they are at least this much further from the previous point
    private static final float MIN_DISTANCE_SQUARED = 10.0e-2f;

    // Used for calculating range color
    private static final float MAX_DISTANCE = 6.0f;

    private final Object mutex;

    private float laserScanDetail;
    private GraphName frame;
    private FloatBuffer vertexFrontBuffer;
    private FloatBuffer vertexBackBuffer;

//    private static final String TAG = "LaserScanLayer";

    public LaserScanLayer(String topicName, float detail) {
        this(GraphName.of(topicName), detail);
    }

    public LaserScanLayer(GraphName topicName, float detail) {
        super(topicName, sensor_msgs.LaserScan._TYPE);
        mutex = new Object();
        this.laserScanDetail = Math.max(detail, 1);
    }

    @Override
    public void draw(VisualizationView view, GL10 gl)
    {
        if (vertexFrontBuffer != null)
        {
            synchronized (mutex)
            {
//                Vertices.drawTriangleFan(gl, vertexFrontBuffer, FREE_SPACE_COLOR);
                drawPoints(gl, vertexFrontBuffer, 0.0f, true);
                // Drop the first point which is required for the triangle fan but is
                // not a range reading.
                FloatBuffer pointVertices = vertexFrontBuffer.duplicate();
                pointVertices.position(3 + 4);
//                Vertices.drawPoints(gl, pointVertices, OCCUPIED_SPACE_COLOR, LASER_SCAN_POINT_SIZE);
                drawPoints(gl, pointVertices, LASER_SCAN_POINT_SIZE, false);

            }
        }
    }

    public static void drawPoints(GL10 gl, FloatBuffer vertices, float size, boolean fan) {
        vertices.mark();

        if (!fan)
            gl.glPointSize(size);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glVertexPointer(3, GL10.GL_FLOAT, (3 + 4) * 4, vertices);

        FloatBuffer colors = vertices.duplicate();
        colors.position(3);
        gl.glColorPointer(4, GL10.GL_FLOAT, (3 + 4) * 4, colors);

        gl.glDrawArrays(fan ? GL10.GL_TRIANGLE_FAN : GL10.GL_POINTS, 0, countVertices(vertices, 3 + 4));

        if (!fan)
        {
            gl.glDrawArrays(GL10.GL_POINTS, 0, countVertices(vertices, 3 + 4));
        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        vertices.reset();
    }

    private static int countVertices(FloatBuffer vertices, int size) {
        Preconditions.checkArgument(vertices.remaining() % size == 0, "Number of vertices: " + vertices.remaining());
        return vertices.remaining() / size;
    }

    @Override
    public void onStart(VisualizationView view, ConnectedNode connectedNode) {
        super.onStart(view, connectedNode);
        Subscriber<LaserScan> subscriber = getSubscriber();
        subscriber.addMessageListener(new MessageListener<LaserScan>() {
            @Override
            public void onNewMessage(LaserScan laserScan) {
                frame = GraphName.of(laserScan.getHeader().getFrameId());
                updateVertexBuffer(laserScan);
            }
        });
    }

    private void updateVertexBuffer(LaserScan laserScan)
    {
        float[] ranges = laserScan.getRanges();
        int size = ((ranges.length) + 2) * (3 + 4);//((ranges.length / stride) + 2) * 3;

        if (vertexBackBuffer == null || vertexBackBuffer.capacity() < size)
        {
            vertexBackBuffer = Vertices.allocateBuffer(size);
        }

        vertexBackBuffer.clear();

        // We start with the origin of the triangle fan.
        vertexBackBuffer.put(0);
        vertexBackBuffer.put(0);
        vertexBackBuffer.put(0);

        // Color
        vertexBackBuffer.put(FREE_SPACE_COLOR.getRed());
        vertexBackBuffer.put(FREE_SPACE_COLOR.getGreen());
        vertexBackBuffer.put(FREE_SPACE_COLOR.getBlue());
        vertexBackBuffer.put(0.1f);

//        float minimumRange = laserScan.getRangeMin();
//        float maximumRange = laserScan.getRangeMax();
        float angle = laserScan.getAngleMin();
        float angleIncrement = laserScan.getAngleIncrement();

        float x, y, xp, yp = xp = 0.0f;
        int num = 0;
        float p;

        // Calculate the coordinates of the laser range values.
        for (int i = 0; i < ranges.length; ++i)
        {
            // Makes the preview look nicer by eliminating round off errors on the last angle
            if (i == ranges.length - 1)
                angle = laserScan.getAngleMax();

            // x, y, z
            x = (float) (ranges[i] * Math.cos(angle));
            y = (float) (ranges[i] * Math.sin(angle));

            p = ranges[i];

            if (p > MAX_DISTANCE)
            {
                p = 1.0f;
            }
            else
            {
                p /= MAX_DISTANCE;
            }

            if (i == 0 || i == ranges.length - 1 || (/*ranges[i] < maximumRange &&*/
                    ((x - xp) * (x - xp) + (y - yp) * (y - yp))
                            > (1.0f/(this.laserScanDetail*this.laserScanDetail))*MIN_DISTANCE_SQUARED))
            {
                vertexBackBuffer.put(x);
                vertexBackBuffer.put(y);
                vertexBackBuffer.put(0.0f);

                // Color
                vertexBackBuffer.put(p * FREE_SPACE_COLOR.getRed() + (1.0f - p));
                vertexBackBuffer.put(p * FREE_SPACE_COLOR.getGreen());
                vertexBackBuffer.put(p * FREE_SPACE_COLOR.getBlue());
                vertexBackBuffer.put(0.1f);

                xp = x;
                yp = y;
                ++num;
            }

            angle += angleIncrement;// * stride;
        }

        vertexBackBuffer.rewind();
        vertexBackBuffer.limit(num * (3 + 4));

        synchronized (mutex)
        {
            FloatBuffer tmp = vertexFrontBuffer;
            vertexFrontBuffer = vertexBackBuffer;
            vertexBackBuffer = tmp;
        }
    }

    @Override
    public GraphName getFrame() {
        return frame;
    }
}