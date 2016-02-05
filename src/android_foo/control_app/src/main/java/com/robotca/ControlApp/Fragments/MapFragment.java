package com.robotca.ControlApp.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.robotca.ControlApp.Core.RobotGPSSub;
import com.robotca.ControlApp.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;


public class MapFragment extends RosFragment {

    public MapFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, null);
        MapView mapView = (MapView) view.findViewById(R.id.mapview);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(true);
        mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);


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

        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(getActivity(), new RobotGPSSub(), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();

        mapView.getOverlays().add(myLocationOverlay);
        IMapController mapViewController = mapView.getController();
        mapViewController.setZoom(18);


        //location.getLatitude();
        //location.getLongitude();
        //location.set();
        //location.set(Location l);
        //location.setLatitude();
        //location.setLongitude();

        /*mapView.getOverlays().add(myLocationOverlay);
        IMapController mapViewController = mapView.getController();
        mapViewController.setZoom(16);
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        double lattitude = (double)lat;
        double longitude = (double)lng;
        GeoPoint me = new GeoPoint(lat, lng);*/
        //mapViewController.setCenter(me);

        //Location location = null;
        //int lat = (int) (location.getLatitude() * 1E6);
        //int lng = (int) (location.getLongitude() * 1E6);

        //GeoPoint point = new GeoPoint(lat, lng);
        //mapView.getController().setCenter(point);

        //mapView.getController().animateTo(point);
        //mapView.invalidate();


     /*public void followRobot(){
         Location robot = null;
         //setLocation(robot);
         mGeoPoint.setLatitudeE6((int) (robotLocation.getLatitude() * 1E6));
         mGeoPoint.setLongitudeE6((int) (robotLocation.getLongitude() * 1E6));
         mMapController.animateTo(mGeoPoint);

         if(mapView != null)  {
             mapView.postInvalidate();
         }
     }*/

        return view;
    }

    public void shutdown(){
    }
}
