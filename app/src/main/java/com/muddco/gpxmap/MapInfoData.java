package com.muddco.gpxmap;

import android.graphics.Bitmap;
import android.net.Uri;

public class MapInfoData {
    private int count;
    private String uri;
    private Bitmap bitmap;

    MapInfoData() {
    }

    int getCount() {
        return count;
    }

    void setCount(int count) {
        this.count = count;
    }

    public Uri getUri() {
        return Uri.parse(uri);
    }

    void setUri(Uri uri) {
        this.uri = uri.toString();
    }

    Bitmap getBitmap() {
        return bitmap;
    }

    void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
