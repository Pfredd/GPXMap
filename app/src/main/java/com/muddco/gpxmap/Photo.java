package com.muddco.gpxmap;

import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDateTime;

public class Photo {
    private String fileName = null;
    private LocalDateTime date = null;
    private long lat = 0;
    private long lon = 0;
    private LatLng positiuon;

    public void add(String pfname) {
        fileName = pfname;
    }

    public String getFname() {
        return fileName;
    }

    public void setDate(LocalDateTime mdate) {
        date = mdate;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setLatLon(long mlat, long mlon) {
        lat = mlat;
        lon = mlon;
    }

    public long getLat() {
        return lat;
    }

    public long getLon() {
        return lon;
    }

    public void setPosition(LatLng mposition) {
        positiuon = mposition;
    }

    public LatLng getPositiuon() {
        return positiuon;
    }


    /*public int compareTo(Photo p) {
        if (this.getDate().equals(p.getDate())
            return 0;
        return this.getDate().compareTo(p.getDate());
   }*/
}
