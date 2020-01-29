package com.muddco.gpxmap;

import android.graphics.Color;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

public class MapFragment extends Fragment {

    private static GoogleMap mMap = null;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        rootView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            //
            // Listen for mouse wheel events.
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                if (mMap != null && (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER))) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_SCROLL:
                            if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
                                mMap.animateCamera(CameraUpdateFactory.zoomOut());
                            else
                                mMap.animateCamera(CameraUpdateFactory.zoomIn());
                            return true;
                    }
                }
                return false;
            }
        });


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

            }
        });

        return rootView;
    }

    static public void displayTrack(TrackData tData) {
        Double pLat, pLon;
        LatLng position;
        PolylineOptions polyLine = new PolylineOptions();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        polyLine.geodesic(false);   // Not needed on our small scale map
        polyLine.width(10);
        polyLine.color(Color.RED);

        ArrayList<TrackPoint> points = tData.getTrackPoints();
        for (TrackPoint trackPoint : points) {
            pLat = trackPoint.getLatitude();
            pLon = trackPoint.getLongitude();
            position = new LatLng(pLat, pLon);
            polyLine.add(position);     // Add track point to map
            builder.include(position);  // Save track polint for map scaling
        }
        // Scale map so the entire track is shown
        LatLngBounds bounds = builder.build();
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
        mMap.addPolyline(polyLine);
        mMap.animateCamera(cu);
    }

    static public void displayPhotos(ArrayList<Photo> photoList) {

        for (Photo photo : photoList) {
            mMap.addMarker(new MarkerOptions()
                    .position(photo.getPositiuon())
                    .title(photo.getFname()));
        }
    }
}
