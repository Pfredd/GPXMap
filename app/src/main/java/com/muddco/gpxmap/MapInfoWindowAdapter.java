package com.muddco.gpxmap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context;

    MapInfoWindowAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        return null;
    }

    @Override
    public View getInfoContents(Marker arg0) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = null;
        try {
            assert inflater != null;
            v = inflater.inflate(R.layout.map_info_window, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

//            LatLng latLng = arg0.getPosition();
//            TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
//            TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
//            tvLat.setText("Latitude:" + latLng.latitude);
//            tvLng.setText("Longitude:"+ latLng.longitude);
        return v;
    }
}

