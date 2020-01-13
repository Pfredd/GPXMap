package com.muddco.gpxmap;

import com.muddco.gpxmap.gpxparser.TrackPoint;

import java.util.ArrayList;

public class TrackData {
    private static ArrayList<TrackPoint> trackPoints;

    public TrackData() {
        trackPoints = new ArrayList<TrackPoint>();
    }

    public ArrayList<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    void addTrackPoint(TrackPoint tp) {
        trackPoints.add(tp);
    }
}
