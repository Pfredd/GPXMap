package com.muddco.gpxmap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

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
    public View getInfoContents(Marker marker) {
        Gson gson = new Gson();
        View v = null;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {
            assert inflater != null;
            v = inflater.inflate(R.layout.map_info_window, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MarkerData markerData = gson.fromJson((String) marker.getTag(), MarkerData.class);
        ImageView imageView = v.findViewById(R.id.image);
        Glide.with(context)
                .load(markerData.getPhotoUri())
                .fitCenter()
                .into(imageView);

        TextView title = v.findViewById(R.id.name);
        title.setText(marker.getTitle());

        TextView desc = v.findViewById(R.id.desc);
        desc.setText((markerData.getPhotoiLatlng().toString()));
//        InputStream inputStream = null;
//        try {
//            inputStream = context.getContentResolver().openInputStream(uri);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
//        imageView.setImageBitmap(myBitmap);
//        String path = uri.getPath();
//        File file = new File(path);
//
//            LatLng latLng = arg0.getPosition();
//            TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
//            TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
//            tvLat.setText("Latitude:" + latLng.latitude);
//            tvLng.setText("Longitude:"+ latLng.longitude);
        return v;
    }
}

