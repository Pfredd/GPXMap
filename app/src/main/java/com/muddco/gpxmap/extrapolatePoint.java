package com.muddco.gpxmap;

public class extrapolatePoint {

    private Double lat1;
    private Double lon1;
    private Double lat2;
    private Double lon2;
    private  float percent;

    public   extrapolatePoint(Double la1, Double lo1, Double la2, Double lo2, float pct) {

        lat1 = la1;
        lon1 = lo1;
        lat2 = la2;
        lon2 = lo2;
        percent = pct;
    }

    public Double latitude() {
        return lat1 + ((lat2-lat1)*percent);
    }

    public Double longitude() {
        return lon1 + ((lon2-lon1)*percent);
    }

}
