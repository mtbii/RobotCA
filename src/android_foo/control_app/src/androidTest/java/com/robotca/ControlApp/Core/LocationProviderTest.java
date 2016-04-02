package com.robotca.ControlApp.Core;


import android.location.Location;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.ros.internal.message.RawMessage;

import sensor_msgs.NavSatFix;
import sensor_msgs.NavSatStatus;
import std_msgs.Header;

import static org.junit.Assert.*;

/**
 * Test for LocationProvider.
 *
 * Created by Nathaniel on 4/2/16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocationProviderTest {

    private LocationProvider locationProvider;
    private IMyLocationConsumer consumer;

    private static boolean locationChanged;

    @Before
    public void setUp() throws Exception {

        // Create LocationProvider to test
        locationProvider = new LocationProvider();

        // Create consumer to test with
        consumer = new IMyLocationConsumer() {
            @Override
            public void onLocationChanged(Location location, IMyLocationProvider source) {
                locationChanged = true;
            }
        };
    }

    @Test
    public void A_testStartLocationProvider() throws Exception {

        assertEquals(true, locationProvider.startLocationProvider(consumer));
    }

    @Test
    public void C_testGetLastKnownLocation() throws Exception {
        assertEquals(0.0, locationProvider.getLastKnownLocation().getLatitude(), 0.001);
        assertEquals(0.0, locationProvider.getLastKnownLocation().getLongitude(), 0.001);
    }

    @Test
    public void B_testOnNewMessage() throws Exception {
        locationProvider.startLocationProvider(consumer);

        locationProvider.onNewMessage(new NavSatFix() {
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

        assertEquals(true, locationChanged);
    }
}