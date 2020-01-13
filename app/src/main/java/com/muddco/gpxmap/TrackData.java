package com.muddco.gpxmap;

import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

import java.util.ArrayList;

 class TrackData {
    private static ArrayList<TrackPoint> trackPoints;

    TrackData() {
        trackPoints = new ArrayList<>();
    }

     ArrayList<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    void addTrackPoint(TrackPoint tp) {
        trackPoints.add(tp);
    }
}
