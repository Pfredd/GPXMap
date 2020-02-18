package com.muddco.gpxmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private static ArrayList<MapInfoData> infodata = new ArrayList<>();
    private static Context context;
    private static int infoCount = 0;

    MapInfoWindowAdapter(Context context) {
        MapInfoWindowAdapter.context = context.getApplicationContext();
    }

    static int AddMapInfoData(Uri uri) {
        MapInfoData idata = new MapInfoData();
        RequestBuilder<Bitmap> bmap = null;

        idata.setCount(infoCount);
        idata.setUri(uri);

        Glide.with(context)
                .asBitmap()
                .load(uri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        idata.setBitmap(resource);
                        infodata.add(idata);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });

        return infoCount++;
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

        int count = (int) marker.getTag();
        MapInfoData idata = infodata.stream()
                .filter(data -> count == data.getCount())
                .findAny()
                .orElse(null);

        ImageView imageView = null;
        if (v != null) {
            imageView = v.findViewById(R.id.image);
            imageView.setImageBitmap(Objects.requireNonNull(idata).getBitmap());

            TextView title = v.findViewById(R.id.name);
            title.setText(marker.getTitle());

            TextView desc = v.findViewById(R.id.desc);
            desc.setText(marker.getSnippet());
        }

        return v;
    }
}

