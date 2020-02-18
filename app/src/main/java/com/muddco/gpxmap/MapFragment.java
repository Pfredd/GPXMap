package com.muddco.gpxmap;

import android.content.Context;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

public class MapFragment extends Fragment {

    private static GoogleMap mMap = null;
    private static ArrayList<Photo> pData = null;
    private static Context context;


    public MapFragment() {
        // Required empty public constructor
    }

    static void displayPhotos(ArrayList<Photo> photoList) {
        MarkerData mData;
        Gson gson = new Gson();
        String jsonString;

        for (Photo photo : photoList) {
            mData = new MarkerData();
            mData.setPhotoUri(photo.getUri());
            mData.setPhotoLatLng(photo.getPosition());
            jsonString = gson.toJson(mData);

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(photo.getPosition())
                    .title(photo.getFname()));
            marker.setTag(jsonString);

        }
    }

    static void displayTrack(TrackData tData) {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        // Setting a custom info window adapter for the google map

        //
// Listen for mouse wheel events.
        rootView.setOnGenericMotionListener((v, event) -> {
            if (mMap != null && (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER))) {
                if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                    if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
                        mMap.animateCamera(CameraUpdateFactory.zoomOut());
                    else
                        mMap.animateCamera(CameraUpdateFactory.zoomIn());
                    return true;
                }
            }
            return false;
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frg);
        //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        assert mapFragment != null;
        mapFragment.getMapAsync(gMap -> {

            Double pLat, pLon;
            LatLng position;
            PolylineOptions polyLine = new PolylineOptions();

            mMap = gMap;
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            mMap.clear(); //clear old markers

            // Install custom map info window handler
            MapInfoWindowAdapter mapInfoWindowAdapter = new MapInfoWindowAdapter(Objects.requireNonNull(getActivity()));
            mMap.setInfoWindowAdapter(mapInfoWindowAdapter);
        });

        return rootView;
    }
}
