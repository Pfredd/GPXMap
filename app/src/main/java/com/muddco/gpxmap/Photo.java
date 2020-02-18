package com.muddco.gpxmap;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDateTime;

public class Photo {
    private String fileName;
    private Uri uri;
    private LocalDateTime date = null;
    private LatLng position;

    Photo(String pfname) {
        fileName = pfname;
    }

    Uri getUri() {
        return uri;
    }

    void setUri(Uri mUri) {
        uri = mUri;
    }

    String getFname() {
        return fileName;
    }

    void setDate(LocalDateTime mdate) {
        date = mdate;
    }

    public LocalDateTime getDate() {
        return date;
    }

    LatLng getPosition() {
        return position;
    }

    void setPosition(LatLng mposition) {
        position = mposition;
    }


    /*public int compareTo(Photo p) {
        if (this.getDate().equals(p.getDate())
            return 0;
        return this.getDate().compareTo(p.getDate());
   }*/
}
