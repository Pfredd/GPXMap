package com.muddco.gpxmap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.io.InputStream;

public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private static Context context;

    MapInfoWindowAdapter(Context context) {
        MapInfoWindowAdapter.context = context.getApplicationContext();
    }

    @Override
    public View getInfoWindow(Marker arg0) {

        return null;
    }

    static private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getInfoContents(Marker marker) {
        View v = null;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {
            assert inflater != null;
            v = inflater.inflate(R.layout.map_info_window, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri uri = Uri.parse((String) marker.getTag());

        ImageView imageView;
        if (v != null) {
            imageView = v.findViewById(R.id.image);
            Bitmap bm = null;
            try {
                bm = decodeSampledBitmapFromUri(uri, 200, 200);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(bm);

            TextView title = v.findViewById(R.id.name);
            title.setText(marker.getTitle());

            TextView desc = v.findViewById(R.id.desc);
            desc.setText(marker.getSnippet());
        }

        return v;
    }

    private Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight) throws IOException {

        // First decode with inJustDecodeBounds=true to obtain dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(inputStream, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        // "rewind" InputStream
        inputStream.close();
        inputStream = context.getContentResolver().openInputStream(uri);

        Bitmap bm = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();
        return bm;
    }
}

