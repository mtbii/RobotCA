package com.robotca.ControlApp.Layers;

import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.Core.Utils;

import org.ros.android.view.visualization.Color;
import org.ros.android.view.visualization.Vertices;
import org.ros.message.MessageListener;
import org.ros.rosjava_geometry.Vector3;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import sensor_msgs.LaserScan;

/**
 * Renderer for rendering LaserScan data. Supports panning and zooming.
 *
 * Created by Nathaniel Stone on 3/29/16.
 */
public class LaserScanRenderer implements GLSurfaceView.Renderer, MessageListener<LaserScan> {

    // Base color of the laser scan
    private static final Color LASER_SCAN_COLOR = Color.fromHexAndAlpha("377dfa", 0.1f);

    // Color of robot position indicator
    private static final Color ROBOT_INDICATOR_COLOR = Color.fromHexAndAlpha("1133FF", 1.0f);

    // Default point size of laser scan points
    private static final float LASER_SCAN_POINT_SIZE = 10.f;

    // Only adds laser scan points if they are at least this much further from the previous point
    private static final float MIN_DISTANCE_SQUARED = 20.0e-2f; // meters

    // Used for calculating range color
    private static final float MAX_DISTANCE = 10.0f; // meters

    // The base cameraZoom amount
    private static final float BASE_ZOOM = 100f;

    // Log tag String
    private static final String TAG = "LaserScanRenderer";

    // The current ControlApp
    private final ControlApp controlApp;

    // Width and height of the surface view
    private int width, height;

    // Camera parameters
    private float cameraZoom = 1.0f;
    private float cameraAngle;
    private float xShift, yShift;

    // Used for panning the view
    private boolean isMoving;
    private float xStart, yStart;
    private float offX, offY;

    // Used for zooming the view
    private final ScaleGestureDetector scaleGestureDetector;
    // Used for detecting taps for placing waypoints
    private final GestureDetector gestureDetector;

    // Controls the density of scan points
    private float laserScanDetail;

    // Lock for synchronizing drawing
    private final Object mutex;

    // Buffer of scan points for drawing
    private FloatBuffer vertexFrontBuffer;

    // Buffer of scan points for updating
    private FloatBuffer vertexBackBuffer;

    /**
     * Creates a LaserScanRenderer.
     * @param controlApp The current ControlApp
     */
    public LaserScanRenderer(ControlApp controlApp) {
        this.controlApp = controlApp;
        this.mutex = new Object();

        this.laserScanDetail = Float.parseFloat(PreferenceManager
                        .getDefaultSharedPreferences(controlApp)
                        .getString("edittext_laser_scan_detail", "1"));

        if (this.laserScanDetail < 1f)
            this.laserScanDetail = 1f;

        setCameraAngle(90.0f);

        this.scaleGestureDetector = new ScaleGestureDetector(controlApp,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {

                        xShift = xStart + 2 * (detector.getFocusX() - width / 2) / (cameraZoom * BASE_ZOOM) + offX;
                        yShift = yStart - 2 * (detector.getFocusY() - height / 2) / (cameraZoom * BASE_ZOOM) + offY;

                        cameraZoom *= detector.getScaleFactor();

                        // Don't let the object get too small or too large
                        if (cameraZoom < 0.1f)
                            cameraZoom = 0.1f;
                        else if (cameraZoom > 5.0f)
                            cameraZoom = 5.0f;

                        return true;
                    }

                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {

                        xStart = -2 * (detector.getFocusX() - width / 2) / (cameraZoom * BASE_ZOOM);
                        yStart = 2 * (detector.getFocusY() - height / 2) / (cameraZoom * BASE_ZOOM);

                        offX = xShift;
                        offY = yShift;

                        return true;
                    }
                });

        this.gestureDetector = new GestureDetector(controlApp, new GestureDetector.SimpleOnGestureListener()
        {
            @Override
            public boolean onDoubleTap(MotionEvent e)
            {
                try {
                    float x = e.getX() - width / 2;
                    float y = e.getY() - height / 2;

                    x = 2 * x / (cameraZoom * BASE_ZOOM);
                    y = 2 * y / (cameraZoom * BASE_ZOOM);

                    x -= xShift;
                    y += yShift;

                    LaserScanRenderer.this.controlApp.addWaypointWithCheck(screenToWorld(x, y), cameraZoom);
                } catch (Exception ex) {
                    // Ignore
                }
                return false;
            }
        });
    }

    /**
     * Recenters the LaserScanLayer.
     */
    public void recenter() {
        isMoving = false;
        xShift = 0.0f;
        yShift = 0.0f;
    }

    /**
     * Callback for touch events to this Layer.
     *
     * @param event The touch MotionEvent
     * @return True if the event was handled successfully and false otherwise
     */
    public boolean onTouchEvent(MotionEvent event) {
        final float s = 2f / (cameraZoom * BASE_ZOOM);

        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        boolean r = true;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_POINTER_DOWN:
                isMoving = false;
                Log.d(TAG, "Pointer Down");
                r = false;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "Pointer Up");
                break;

            case MotionEvent.ACTION_DOWN:
                if (!isMoving) {
                    isMoving = true;
                    xStart = -event.getX() * s;
                    yStart = event.getY() * s;

                    offX = xShift;
                    offY = yShift;
                }
                break;
            case MotionEvent.ACTION_UP:

                if (isMoving) {
                    isMoving = false;
                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (isMoving && !scaleGestureDetector.isInProgress()) {
                    xShift = xStart + event.getX() * s + offX;
                    yShift = yStart - event.getY() * s + offY;
                }
                break;
        }

        return r;
    }

    /**
     * Called when the surface is created or recreated.
     *
     * @param gl the GL interface.
     * @param config the EGLConfig of the created surface. Can be used
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.9f, 0.9f, 0.9f, 1.0f);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glDisable(GL10.GL_DITHER);
    }

    /**
     * Called when the surface changed size.
     *
     * @param gl The GL interface.
     * @param width The width of the view
     * @param height The height of the view
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;

        gl.glViewport(0, 0, width, height);
    }

    /**
     * Called to draw the current frame.
     * @param gl the GL interface. Use <code>instanceof</code> to
     *           test if the interface supports GL11 or higher interfaces.
     */
    @Override
    public void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        if (vertexFrontBuffer != null) {

            final float cameraX = xShift;
            final float cameraY = yShift;

            synchronized (mutex) {

                gl.glPushMatrix();

                // Adjust scale for screen aspect ratio
                gl.glScalef(cameraZoom * BASE_ZOOM / width, cameraZoom * BASE_ZOOM / height, 1f);

                // Apply camera translation
                gl.glTranslatef(cameraX, cameraY, 0.0f);

                // Rotate by the camera angle
                gl.glRotatef(cameraAngle, 0.0f, 0.0f, 1.0f);

//                // Apply camera translation
//                gl.glTranslatef(cameraX, cameraY, 0.0f);

                // Draw start position
                drawPoint(gl, 0.0, 0.0, 32.0f, 0xFFCCCCDD, null);

                // Draw the robot
                Utils.drawShape(gl, ROBOT_INDICATOR_COLOR);

                // Draw the scan area
                Utils.drawPoints(gl, vertexFrontBuffer, 0.0f, true);

                // Drop the first point which is required for the triangle fan but is
                // not a range reading.
                FloatBuffer pointVertices = vertexFrontBuffer.duplicate();
                pointVertices.position(3 + 4);

                // Draw the scan points
                Utils.drawPoints(gl, pointVertices, LASER_SCAN_POINT_SIZE, false);

                // Draw waypoints
                drawWayPoints(gl);

                // Reset transforms
                gl.glPopMatrix();
            }
        }

        gl.glDisable(GL10.GL_BLEND);
    }

    /**
     * Callback for receiving LaserScan data.
     *
     * @param laserScan The LaserScan
     */
    public void onNewMessage(LaserScan laserScan) {

        // TODO limit update rate
        updateVertexBuffer(laserScan);

    }

    /**
     * Sets the camera angle.
     * @param cameraAngle The new angle, in degrees
     */
    public void setCameraAngle(float cameraAngle) {
        this.cameraAngle = cameraAngle;
    }

    /**
     * Updates the contents of the vertexBackBuffer to the result of the specified LaserScan.
     * @param laserScan The LaserScan
     */
    private void updateVertexBuffer(LaserScan laserScan) {
        float[] ranges = laserScan.getRanges();
        int size = ((ranges.length) + 2) * (3 + 4);

        if (vertexBackBuffer == null || vertexBackBuffer.capacity() < size) {
            vertexBackBuffer = Vertices.allocateBuffer(size);
        }

        vertexBackBuffer.clear();

        // We start with the origin of the triangle fan.
        vertexBackBuffer.put(0.0f);
        vertexBackBuffer.put(0.0f);
        vertexBackBuffer.put(0.0f);

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

        final float W = width / cameraZoom + Math.abs(xShift);
        final float H = height / cameraZoom + Math.abs(yShift);
        final float MAX_RANGE = (float) Math.sqrt(W * W + H * H);

        boolean draw;
        float scale;

        // Calculate the coordinates of the laser range values.
        for (int i = 0; i < ranges.length; ++i) {
            // Makes the preview look nicer by eliminating round off errors on the last angle
            if (i == ranges.length - 1)
                angle = laserScan.getAngleMax();

            if (ranges[i] > MAX_RANGE)
                ranges[i] = MAX_RANGE;

            // x, y, z
            x = (float) (ranges[i] * Math.cos(angle));
            y = -(float) (ranges[i] * Math.sin(angle));

            p = ranges[i];

            if (p > MAX_DISTANCE) {
                p = 1.0f;
            } else {
                p /= MAX_DISTANCE;
            }

            scale = ranges[i];

            if (scale < 1.0)
                scale = 1.0f;

            draw = ((x - xp) * (x - xp) + (y - yp) * (y - yp))
                    > (scale / (laserScanDetail * laserScanDetail)) * MIN_DISTANCE_SQUARED;

            if (i == 0 || i == ranges.length - 1 || draw) {
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

        synchronized (mutex) {
            FloatBuffer tmp = vertexFrontBuffer;
            vertexFrontBuffer = vertexBackBuffer;
            vertexBackBuffer = tmp;
        }
    }

    /*
     * Draws the way points.
     */
    private void drawWayPoints(GL10 gl) {

        FloatBuffer b;
        PointF res = new PointF();

        // Draw the waypoints
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_GEQUAL);

        // Lock on waypoints to prevent modifications while reading
        synchronized (controlApp.getWaypoints()) {

            b = Vertices.allocateBuffer(3 * controlApp.getWaypoints().size());
            b.rewind();

            for (Vector3 pt : controlApp.getWaypoints()) {

                drawPoint(gl, pt.getX(), pt.getY(), 0.0f, 0, res);

                b.put(res.x);
                b.put(res.y);
                b.put(0.0f);
            }
        }

        b.rewind();

        // Draw the path
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glLineWidth(8.0f);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 3 * 4, b);
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, b.capacity() / 3);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glDisable(GL10.GL_DEPTH_TEST);

        // Draw the Waypoints
        b.rewind();
        for (int i = 0; i < b.limit(); i += 3) {
            Utils.drawPoint(gl, b.get(i), b.get(i + 1), 24.0f, i == 0 ? 0xFF22CC33 : 0xFF2233CC);
        }
    }

    /*
     * Draws a point specified in world space.
     * Pass a non-null PointF to result to grab the converted point instead of drawing it.
     */
    private static void drawPoint(GL10 gl, double x, double y, float size, int color, PointF result) {
        double rx = RobotController.getX();
        double ry = RobotController.getY();

        // Calculations
        double dir = Utils.pointDirection(rx, ry, x, y);
        dir = Utils.angleDifference(dir, RobotController.getHeading());
        double len = Utils.distance(rx, ry, x, y);

        x = Math.cos(dir) * len;
        y = Math.sin(dir) * len;

        if (result != null) {
            result.set((float) x, (float) y);
        } else {
            Utils.drawPoint(gl, (float) x, (float) y, size, color);
        }
    }

    /*
     * Converts a screen point to world space.
     */
    private Vector3 screenToWorld(double sx, double sy) {
        double rx = RobotController.getX();
        double ry = RobotController.getY();

        sy = -sy;

        double cos = Math.cos(RobotController.getHeading() - Math.toRadians(cameraAngle));
        double sin = Math.sin(RobotController.getHeading() - Math.toRadians(cameraAngle));

        double xx = sx * cos - sy * sin;
        double yy = sx * sin + sy * cos;

        sx = rx + xx;
        sy = ry + yy;

        return new Vector3(sx, sy, 0.0);
    }
}
