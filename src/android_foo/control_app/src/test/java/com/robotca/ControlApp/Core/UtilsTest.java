package com.robotca.ControlApp.Core;

import org.junit.Test;
import org.ros.rosjava_geometry.Vector3;

import static org.junit.Assert.*;

/**
 * Unit tests for Utils.
 *
 * Created by Nathaniel Stone on 4/1/16.
 */
public class UtilsTest {

    @Test
    public void testAngleDifference() throws Exception {
        assertEquals(Utils.angleDifference(Math.PI * 8.0, Math.PI), -Math.PI, 0.01);
    }

    @Test
    public void testPointDirection() throws Exception {
        assertEquals(Utils.pointDirection(-1.0, -1.0, 5.0, 5.0), Math.PI / 4.0, 0.01);
    }

    @Test
    public void testDistance() throws Exception {
        assertEquals(Utils.distance(-2.0, -1.0, -1.0, 0.0), 1.41, 0.1);
    }

    @Test
    public void testDistanceSquared() throws Exception {
        assertEquals(Utils.distanceSquared(-2.0, -1.0, -1.0, 0.0), 2.00, 0.1);
    }

    @Test
    public void testDistanceToLine() throws Exception {
        assertEquals(Utils.distanceToLine(0.0, 0.0, new Vector3(1.0, 1.0, 0.0), new Vector3(-1.0, 1.0, 0.0)), 1.0, 0.01);
    }
}