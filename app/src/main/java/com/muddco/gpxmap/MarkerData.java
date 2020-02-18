package com.muddco.gpxmap;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;


public class MarkerData {
    //
    // Data that is stored in the map marker "Tag" field
    // Since the map is in a different fragment, it cant access
    // static data from the main class
    //

    private String photoUriString;
    private LatLng photoLatLng;

    public Uri getPhotoUri() {
        Uri uri = Uri.parse(photoUriString);
        return uri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUriString = photoUri.toString();
    }

    public void setPhotoLatLng(LatLng photoLatLng) {
        this.photoLatLng = photoLatLng;
    }

    public LatLng getPhotoiLatlng() {
        return photoLatLng;
    }
}

