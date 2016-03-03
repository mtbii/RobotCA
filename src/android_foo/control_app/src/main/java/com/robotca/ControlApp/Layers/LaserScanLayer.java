package com.robotca.ControlApp.Layers;

import android.view.MotionEvent;

import com.google.common.base.Preconditions;

import org.ros.android.view.visualization.Color;
import org.ros.android.view.visualization.Vertices;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.SubscriberLayer;
import org.ros.android.view.visualization.layer.TfLayer;
import org.ros.android.view.visualization.shape.PixelSpacePoseShape;
import org.ros.android.view.visualization.shape.Shape;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import sensor_msgs.LaserScan;

/**
 * Improved version of the ros laser scan layer.
 * Instead of using a preset stride to limit the number of points drawn, points are drawn when their
 * distance from the last drawn point exceeds some value.
 *
 * Created by Nathaniel Stone on 2/12/16.
 *
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
public class LaserScanLayer extends SubscriberLayer<LaserScan> implements TfLayer {

    // Base color of the laser scan
    private static final Color LASER_SCAN_COLOR = Color.fromHexAndAlpha("377dfa", 0.1f);

    // Default point size of laser scan points
    private static final float LASER_SCAN_POINT_SIZE = 10.f;

    // Only adds laser scan points if they are at least this much further from the previous point
    private static final float MIN_DISTANCE_SQUARED = 20.0e-2f; // meters

    // Used for calculating range color
    private static final float MAX_DISTANCE = 10.0f; // meters

    // Lock for synchronizing drawing
    private final Object mutex;

    // Controls the density of scan points
    private float laserScanDetail;

    // GraphName for LaserScan
    private GraphName frame;

    // Buffer of scan points for drawing
    private FloatBuffer vertexFrontBuffer;

    // Buffer of scan points for updating
    private FloatBuffer vertexBackBuffer;

    // Used for panning the view
    private boolean isMoving;
    private float xStart, yStart;
    private float xShift, yShift;
    private float offX, offY;

    // Shape to draw to show the robot's position
    private Shape shape;

    @SuppressWarnings("unused")
    private static final String TAG = "LaserScanLayer";

    /**
     * Creates a LaserScanLayer.
     * @param topicName Topic name for laser scanner
     * @param detail Detail of drawn points
     */
    public LaserScanLayer(String topicName, float detail) {
        this(GraphName.of(topicName), detail);
    }

    /**
     * Creates a LaserScanLayer.
     * @param topicName Topic name for laser scanner
     * @param detail Detail of drawn points
     */
    public LaserScanLayer(GraphName topicName, float detail) {
        super(topicName, sensor_msgs.LaserScan._TYPE);
        mutex = new Object();
        this.laserScanDetail = Math.max(detail, 1);

        xShift = yShift = 0.0f;
    }

    /**
     * Recenters the LaserScanLayer.
     */
    public void recenter()
    {
        isMoving = false;
        xShift = 0.0f;
        yShift = 0.0f;
    }

    /**
     * Callback for touch events to this Layer.
     * @param view The touched View
     * @param event The touch MotionEvent
     * @return True indicating the even was handled successfully
     */
    public boolean onTouchEvent(VisualizationView view, MotionEvent event)
    {
        final float s = 1.0f / (float) view.getCamera().getZoom();
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if (!isMoving){
                    isMoving = true;
                    xStart = event.getY() * s;
                    yStart = -event.getX() * s;

                    offX = xShift;
                    offY = yShift;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isMoving){
                    isMoving = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isMoving) {
                    xShift = xStart - event.getY() * s + offX;
                    yShift = yStart + event.getX() * s + offY;
                }
                break;
        }

        return true;
    }

    /**
     * Draws the LaserScan.
     * @param view The VisualizationView this LaserScanLayer is attached to.
     * @param gl The GL10 instance for drawing
     */
    @Override
    public void draw(VisualizationView view, GL10 gl)
    {
        if (vertexFrontBuffer != null)
        {
            synchronized (mutex)
            {
                // Draw the shape
//                shape.getTransform().getTranslation().add(new Vector3(xShift, yShift, 0.0f));
//                shape.getTransform().apply(new Vector3(xShift, yShift, 0.0f));
                gl.glTranslatef(xShift, yShift, 0.0f);
                shape.draw(view, gl);

                // Draw the scan area
                drawPoints(gl, vertexFrontBuffer, 0.0f, true);

                // Drop the first point which is required for the triangle fan but is
                // not a range reading.
                FloatBuffer pointVertices = vertexFrontBuffer.duplicate();
                pointVertices.position(3 + 4);

                // Draw the scan points
                drawPoints(gl, pointVertices, LASER_SCAN_POINT_SIZE, false);

                gl.glTranslatef(-xShift, -yShift, 0.0f);
            }
        }
    }

    /**
     * Draws the contents of the specified buffer.
     * @param gl GL10 object for drawing
     * @param vertices FloatBuffer of vertices to draw
     * @param size Size of draw points
     * @param fan If true, draws the buffer as a triangle fan, otherwise draws it as a point cloud
     */
    public static void drawPoints(GL10 gl, FloatBuffer vertices, float size, boolean fan) {
        vertices.mark();

        if (!fan)
            gl.glPointSize(size);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glVertexPointer(3, GL10.GL_FLOAT, (3 + 4) * 4, vertices);

        FloatBuffer colors = vertices.duplicate();
        colors.position(fan ? 3: 10);
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

    /*
     * Helper function to calculate the number of vertices in a FloatBuffer.
     */
    private static int countVertices(FloatBuffer vertices, int size) {
        Preconditions.checkArgument(vertices.remaining() % size == 0, "Number of vertices: " + vertices.remaining());
        return vertices.remaining() / size;
    }

    /**
     * Initializes the LaserScanLayer.
     * @param view Parent VisualizationView
     * @param connectedNode Node this layer is connected to
     */
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

        this.shape = new PixelSpacePoseShape();
    }

    /*
     * Updates the contents of the vertexBackBuffer to the result of the specified LaserScan.
     */
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
        vertexBackBuffer.put(0.0f);
        vertexBackBuffer.put(0.0f);
        vertexBackBuffer.put(0);

        // Color
        vertexBackBuffer.put(LASER_SCAN_COLOR.getRed());
        vertexBackBuffer.put(LASER_SCAN_COLOR.getGreen());
        vertexBackBuffer.put(LASER_SCAN_COLOR.getBlue());
        vertexBackBuffer.put(0.1f);

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
                vertexBackBuffer.put(p * LASER_SCAN_COLOR.getRed() + (1.0f - p));
                vertexBackBuffer.put(p * LASER_SCAN_COLOR.getGreen());
                vertexBackBuffer.put(p * LASER_SCAN_COLOR.getBlue());
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

    /**
     * Returns this layers' GraphName
     * @return  The GraphName of this Layer
     */
    @Override
    public GraphName getFrame() {
        return frame;
    }
}