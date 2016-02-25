package com.robotca.ControlApp.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.robotca.ControlApp.Core.RobotGPSSub;
import com.robotca.ControlApp.R;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;

import java.util.ArrayList;
import java.lang.Math.*;


public class MapFragment extends Fragment implements MapEventsReceiver {

//    private RobotGPSSub robotGPSNode;
    Button recenterButton;
    MyLocationNewOverlay myLocationOverlay;
    MapView mapView;
    MapEventsOverlay mapEventsOverlay;

    double distanceToTravel;
    ArrayList<GeoPoint> waypoints = new ArrayList<>();

    public MapFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, null);
        mapView = (MapView) view.findViewById(R.id.mapview);
        recenterButton = (Button) view.findViewById(R.id.recenter);
        recenterButton.setOnClickListener(recenterListener);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(true);
        mapView.setTileSource(TileSourceFactory.MAPNIK);


        /*try{
            String m_locale =   Locale.getDefault().getDisplayName();
            BingMapTileSource bing = new BingMapTileSource(m_locale);
            Method m = BingMapTileSource.class.getDeclaredMethod("initMetaData");
            m.setAccessible(true);
            m.invoke(this);
            //BingMapTileSource.initMetaData(this);
            BingMapTileSource.retrieveBingKey(this);

            bing.setStyle(BingMapTileSource.IMAGERYSET_AERIAL);
            mapView.setTileSource(bing);
        }
        catch(Exception e){
            e.printStackTrace();
        }*/

//        robotGPSNode = new RobotGPSSub();

        // Use the RobotGPSSub that the HUDFragment uses
        RobotGPSSub robotGPSNode = ((HUDFragment)getActivity().
                getFragmentManager().findFragmentById(R.id.hud_fragment)).getRobotGPSNode();

        myLocationOverlay = new MyLocationNewOverlay(getActivity(), robotGPSNode, mapView);
        mapEventsOverlay = new MapEventsOverlay(mapView.getContext(), this);

        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();

        mapView.getOverlays().add(myLocationOverlay);
        mapView.getOverlays().add(0, mapEventsOverlay);

        IMapController mapViewController = mapView.getController();
        mapViewController.setZoom(18);

//        nodeMainExecutor.execute(robotGPSNode, nodeConfiguration.setNodeName("android/ros_gps"));
        return view;
    }
    private OnClickListener recenterListener = new OnClickListener() {
        public void onClick(View v) {
            myLocationOverlay.enableFollowLocation();
        }
    };

//    public void shutdown(){
//        nodeMainExecutor.shutdownNodeMain(robotGPSNode);
//    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        Toast.makeText(mapView.getContext(), "Tapped on (" + geoPoint.getLatitude() + "," + geoPoint.getLongitude() + ")", Toast.LENGTH_LONG).show();

        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {

        GroundOverlay myGroundOverlay = new GroundOverlay(getActivity());
        myGroundOverlay.setPosition(geoPoint);
        myGroundOverlay.setImage(getResources().getDrawable(R.drawable.ic_flag_black_24dp).mutate());
        myGroundOverlay.setDimensions(25.0f);
        mapView.getOverlays().add(myGroundOverlay);

        //keep storage of markers and current location
        waypoints.add(myLocationOverlay.getMyLocation());
        waypoints.add(geoPoint);

        Toast.makeText(mapView.getContext(), "Marked on (" + geoPoint.getLatitude() + "," + geoPoint.getLongitude() + ")", Toast.LENGTH_LONG).show();

        return true;
    }

    public double distanceBetweenCoordinates(GeoPoint start, GeoPoint dest) {
        distanceToTravel = 0;
        int earthRadius = 6371000; // m
        double dLat = Math.toRadians(dest.getLatitude()-start.getLatitude());
        double dLon = Math.toRadians(dest.getLongitude()-dest.getLongitude());
        double lat1 = Math.toRadians(start.getLatitude());
        double lat2 = Math.toRadians(dest.getLatitude());

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                      Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        distanceToTravel = earthRadius * c;  // in meters

        Toast.makeText(mapView.getContext(), "Waypoint is " + distanceToTravel + " meters away", Toast.LENGTH_LONG).show();

        return distanceToTravel;
    }
}
