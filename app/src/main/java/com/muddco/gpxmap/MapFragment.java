package com.muddco.gpxmap;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

public class MapFragment extends Fragment {

    private static GoogleMap mMap;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frg);
        //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gMap) {

                Double pLat, pLon;
                LatLng position;
                PolylineOptions polyLine = new PolylineOptions();

                mMap = gMap;
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                mMap.clear(); //clear old markers

                polyLine.geodesic(false);   // Not needed on our small scale map
                polyLine.width(12);
                polyLine.color(Color.RED);
            }
        });

        return rootView;
    }

    static public void displayTrack(TrackData tData) {
        Double pLat, pLon;
        LatLng position;
        boolean firstPoint = true;
        PolylineOptions polyLine = new PolylineOptions();

        ArrayList<TrackPoint> points = tData.getTrackPoints();
        for (TrackPoint trackPoint : points) {
            pLat = trackPoint.getLatitude();
            pLon = trackPoint.getLongitude();
            position = new LatLng(pLat, pLon);
            if (firstPoint) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
                firstPoint = false;
            }
            polyLine.add(position);
        }
        mMap.addPolyline(polyLine);
    }

    static public void displayPhotos(ArrayList<Photo> photoList) {

        for (Photo photo : photoList) {
            mMap.addMarker(new MarkerOptions()
                    .position(photo.getPositiuon())
                    .title("Hello world\n"));
        }
    }
}
