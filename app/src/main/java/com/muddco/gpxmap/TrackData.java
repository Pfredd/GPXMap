package com.muddco.gpxmap;

import java.util.ArrayList;

import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

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
