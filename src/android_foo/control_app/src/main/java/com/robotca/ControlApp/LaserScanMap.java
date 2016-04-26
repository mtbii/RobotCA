package com.robotca.ControlApp;

import android.util.Log;

import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.Layers.LaserScanRenderer;

import org.ros.message.MessageListener;
import org.ros.rosjava_geometry.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import sensor_msgs.LaserScan;

/**
 * Data structure for storing a map made from laser scan data.
 *
 * Created by Nathaniel on 4/25/16.
 */
public class LaserScanMap implements MessageListener<LaserScan> {

    // Log tag String
    private static final String TAG = "LaserScanMap";

    // Map of all existing Quadtrees
    private HashMap<Long, Quadtree> map;

    // Minimum size for a Quad
    private static final float MIN_SIZE = 1.0f;
    // Maximum size for a Quad
    private static final float MAX_SIZE = 32.0f;

    // Last time of a LaserScan message
    private long lastMessageTime;
    // Time to wait between laser scan messages
    private static final long MESSAGE_DELAY = 250L;

    // Maximum number of points in a Quad before it has to be subdivided
    private static final int POINT_LIMIT = 5;

    /**
     * Default Constructor.
     */
    public LaserScanMap()
    {
        map = new HashMap<>();
    }

    /**
     * Callback for when new LaserScan data is received.
     * @param laserScan The new LaserScan
     */
    @Override
    public void onNewMessage(LaserScan laserScan) {

        long time = System.currentTimeMillis();

        if (time - lastMessageTime > MESSAGE_DELAY) {

            lastMessageTime = time;

            float[] ranges = laserScan.getRanges();
            float angle = laserScan.getAngleMin();
            float x, y;
            float rx = (float) RobotController.getX();
            float ry = (float) RobotController.getY();
            float rdir = (float) RobotController.getHeading();

            // Calculate the coordinates of the laser range values.
            for (float range : ranges) {

                if (range < laserScan.getRangeMax() - 1.0f) {
                    // x, y, z
                    x = rx + (float) (range * Math.cos(angle - rdir));
                    y = ry + (float) (range * Math.sin(angle - rdir));

                    addPoint(x, y);
                }
            }
        }
    }

    /**
     * Draws the LaserScanMap.
     * @param gl10 The GL10 on which to draw
     * @param size Size of points
     * @param color Color of points
     */
    public void draw(GL10 gl10, float size, int color)
    {
        for (Map.Entry<Long, Quadtree> entry: map.entrySet())
        {
            entry.getValue().draw(gl10, size, color);
        }
    }

    /*
     * Adds a new scan point to the map.
     */
    private void addPoint(float x, float y)
    {
        ScanPoint pt = new ScanPoint(x, y);
        long key = getKey(pt.x, pt.y);

        if (!map.containsKey(key)) {

            float qx, qy;
            qx = (float) (pt.x < 0.0f ? Math.ceil(pt.x / MAX_SIZE): Math.floor(pt.x / MAX_SIZE));
            qy = (float) (pt.y < 0.0f ? Math.ceil(pt.y / MAX_SIZE): Math.floor(pt.y / MAX_SIZE));
            qx *= MAX_SIZE;
            qy *= MAX_SIZE;
//            qx += MAX_SIZE / 2.0f;
//            qy += MAX_SIZE / 2.0f;

            map.put(key, new Quadtree(MAX_SIZE, POINT_LIMIT, qx, qy));
        }

        map.get(key).add(pt);
    }

    /*
     * Generates a key for the specified position.
     */
    private static long getKey(float x, float y)
    {
        return ((long) (x / MAX_SIZE)) << 32 | ((long) (y / MAX_SIZE));
    }

    /**
     * Quadtree for efficiently storing laser scan data.
     */
    private static class Quadtree
    {
        private static final int N = 0, S = 1, E = 1, W = 0;

        private float size; // The size of the quad
        private int pointLimit; // The maximum number of points per quad
        private float x, y; // The position of the center of the quad

        private Quadtree[][] children; // Children

        private final ArrayList<ScanPoint> points; // List of points

        /*
         * Creates a Quadtree.
         */
        private Quadtree(float size, int pointLimit)
        {
            this.size = size;
            this.x = this.size / 2;
            this.y = this.size / 2;
            this.pointLimit = pointLimit;
            this.children = new Quadtree[2][2];
            this.points = new ArrayList<>();
        }

        /*
         * Constructor used for subdividing a Quadtree.
         */
        private Quadtree(float size, int pointLimit, float x, float y)
        {
            this.size = size;
            this.pointLimit = pointLimit;
            this.x = x;
            this.y = y;
            this.children = new Quadtree[2][2];
            this.points = new ArrayList<>();
        }

        /**
         * Draws this Quadtree.
         * @param gl10 The GL10 on which to draw
         * @param size Size of points
         * @param color Color of points
         */
        public void draw(GL10 gl10, float size, int color)
        {
            for (ScanPoint pt: points)
            {
                LaserScanRenderer.drawPoint(gl10, pt.x, pt.y, size, color, null);
            }

            LaserScanRenderer.drawPoint(gl10, x, y, size*2, 0x88FFFFFF, null);
            LaserScanRenderer.drawPoint(gl10, x + this.size, y, size*2, 0x88FFFFFF, null);
            LaserScanRenderer.drawPoint(gl10, x, y + this.size, size*2, 0x88FFFFFF, null);
            LaserScanRenderer.drawPoint(gl10, x + this.size, y + this.size, size*2, 0x88FFFFFF, null);

            // Draw the children quads
            if (children[0][0] != null)
            {
                children[0][0].draw(gl10, size, color);
                children[0][1].draw(gl10, size, color);
                children[1][0].draw(gl10, size, color);
                children[1][1].draw(gl10, size, color);
            }
        }

        /**
         * Returns true if 'p' is contained in this quad
         */
        public boolean contains(ScanPoint p)
        {
            return p.x > x && p.y > y && p.x < x + size && p.y < y + size;
//            return Math.abs(x - p.x) < size / 2 && Math.abs(y - p.y) < size / 2;
        }

        /**
         * Adds 'p' to this quad
         * @return true if 'p' is added successfully, false otherwise
         */
        public boolean add(ScanPoint p)
        {
            if (!contains(p)) {
                Log.d(TAG, this + " does not contain " + p);
                return false;
            }

            if (size <= MIN_SIZE && points.size() >= pointLimit)
                return true;

            if (children[N][W] == null) // Hasn't reached point limit yet
                points.add(p);
            else
                return children[(y - p.y < size / 2) ? S: N][(x - p.x < size / 2) ? E: W].add(p); // Add point to child quad

            if (points.size() <= pointLimit)
                return true;
            else // Need to subdivide this quad and add points to its children
            {
                children[N][W] = new Quadtree(size / 2, pointLimit, x, y);
                children[N][E] = new Quadtree(size / 2, pointLimit, x + size / 2, y);
                children[S][W] = new Quadtree(size / 2, pointLimit, x, y + size / 2);
                children[S][E] = new Quadtree(size / 2, pointLimit, x + size / 2, y + size / 2);

                boolean success = true;
                for (ScanPoint point: points) // Move points to children
                    success &= add(point);

                points.clear(); // Relinquish memory

                return success;
            }
        }

        @Override
        public String toString()
        {
            return "Quad: " + size + "x" + size + " at (" + x + ", " + y + "): " + (points.size()) + " pts";
        }
    }

    /**
     * Class representing a ScanPoint from the LaserScan.
     */
    private static class ScanPoint
    {
        private float x, y; // The position of the point

        /**
         * Creates a ScanPoint.
         * @param x The x position of the point
         * @param y The y position of the point
         */
        public ScanPoint(float x, float y)
        {
            this.x = x;
            this.y = y;
        }

        /**
         * Creates a ScanPoint from a Vector.
         * @param v The Vector
         */
        public ScanPoint(Vector3 v)
        {
            x = (float) v.getX();
            y = (float) v.getY();
        }

        @Override
        public String toString()
        {
            return "(" + x + ", " + y + ")";
        }
    }
}
