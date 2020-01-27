package com.muddco.gpxmap;

import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDateTime;

public class Photo {
    private String fileName = null;
    private LocalDateTime date = null;
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
