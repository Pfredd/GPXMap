package com.muddco.gpxmap;

import androidx.fragment.app.FragmentActivity;

import android.content.res.AssetManager;
import android.graphics.Color;
// For androidx.exifinterface  - need to add dependancy for
// androidx.exifinterface:exifinterface:1.1.0
import androidx.exifinterface.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.UiSettings;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeFieldType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    static final String TAG = "TEST123"; // GPXParserSampleActivity.class.getSimpleName();

    GPXParser mParser = new GPXParser();

    TrackData tData = new TrackData();
    String gpxFileName = "20190710.gpx";
    String photoFileName = "images/DSC01660.JPG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        try {
            mapFragment.getMapAsync(this);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Double pLat, pLon;
        LatLng position;
        PolylineOptions polyLine = new PolylineOptions();
        Double previousLat = 0.0;
        Double previousLon = 0.0;
        DateTime previousTime = null;
        DateTime pointdt;
        Double tagLat = 0.0;
        Double tagLon = 0.0;


        polyLine.geodesic(false);   // Not needed on our small scale map
        polyLine.width(12);
        polyLine.color(Color.RED);

        Gpx parsedGpx = null;

        long previousMillis = 0;
        long photoMillis = 0;
        long nextMillis = 0;

        InputStream in;
        try {
            in = getAssets().open(gpxFileName);
            parsedGpx = mParser.parse(in);
            in.close();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            Log.e(TAG, "GPX file not found: " + e.getLocalizedMessage());
            int qq = 3;
        }

        if (parsedGpx != null) {
            List<Track> tracks = parsedGpx.getTracks();
            for (int i = 0; i < tracks.size(); i++) {
                Track track = tracks.get(i);
                List<TrackSegment> segments = track.getTrackSegments();
                int kk = 0;
                for (int j = 0; j < segments.size(); j++) {
                    TrackSegment segment = segments.get(j);
                    boolean firstPoint = true;
                    for (TrackPoint trackPoint : segment.getTrackPoints()) {
                        tData.addTrackPoint(trackPoint);
                        pLat = trackPoint.getLatitude();
                        pLon = trackPoint.getLongitude();
                        position = new LatLng(pLat, pLon);
                        if (firstPoint) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
                            firstPoint = false;
                        }
                        polyLine.add(position);
                    }
                    mMap.addPolyline(polyLine);
                }
            }

        } else {
            Log.e(TAG, "Error parsing gpx track!");
            System.exit(1);
        }

        ArrayList<TrackPoint> points = tData.getTrackPoints();
        int h1 = 3;
        String photoDate = getPhotoDate(photoFileName);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy:MM:dd HH:mm:ss");
        DateTime photodt = formatter.parseDateTime(photoDate);
        for (TrackPoint trackPoint : points) {
            pointdt = trackPoint.getTime();
            if (!photodt.isBefore(pointdt) && !photodt.isEqual(pointdt)) {
                Boolean test = photodt.isEqual(pointdt);
                previousLat =trackPoint.getLatitude();
                previousLon = trackPoint.getLongitude();
                previousTime = trackPoint.getTime();
                int r =243;
            } else {
                int k=343;
                try {
                 previousMillis = previousTime.getMillis();
                 photoMillis = photodt.getMillis();
                 nextMillis = pointdt.getMillis();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                if (photoMillis != nextMillis) {
                    // Photo was taken between track data points. So we have to extrapolate the position
                    float pct = (float)(photoMillis - previousMillis) / (float)(nextMillis - previousMillis);
                    extrapolatePoint midPoint = new extrapolatePoint(previousLat, previousLon, trackPoint.getLatitude(), trackPoint.getLongitude(), pct);
                    tagLat = midPoint.latitude();
                    tagLon = midPoint.longitude();
                } else {
                    // Photo was taken at a track point locatrion
                    tagLat = trackPoint.getLatitude();
                    tagLon = trackPoint.getLongitude();
                    int q=342;

                }
                int l=903;
                break;
            }
            int m = 932;
        }
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(tagLat, tagLon))
                .title("Hello world"));

    }

    private String getPhotoDate(String fileName) {
        InputStream mfile;
        String ret = null;

        try {
            AssetManager assetManager = getAssets();
            mfile = assetManager.open(fileName);
            ExifInterface exif = new ExifInterface(mfile);
            ret = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
            String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            mfile.close();
        } catch (IOException e) {
            Log.e(TAG, "Photo file not found: " + e.getLocalizedMessage());
        }

        return (ret);

    }

    private void setGPS(String filePath, Double latitude, Double longitude) {

        try {
            ExifInterface exif = new ExifInterface(filePath);
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPS.convert(latitude));
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPS.latitudeRef(latitude));
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPS.convert(longitude));
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPS.longitudeRef(longitude));
        exif.saveAttributes();
        } catch (Exception e) {
            // if any I/O error occurs
            e.printStackTrace();

        }
    }

    private void test123() {
        InputStream mfile;
        String LOG_ID = "TEST123";

        try {
            //AssetManager assetManager = getAssets();
            mfile = getAssets().open("images/DSC01638.JPG");
            //mfile = assetManager.open("images/test1.jpg");
            ExifInterface exif = new ExifInterface(mfile);
            geoDegree gd = new geoDegree(exif);
            Log.e(LOG_ID, "LATITUDE EXTRACTED: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            Log.e(LOG_ID, "LONGITUDE EXTRACTED: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
            Log.e(LOG_ID, "gd.isValid: " + gd.isValid());
            Log.e(LOG_ID, "gd.toString: " + gd.toString());
            Log.e(LOG_ID, "DATETIME: " + exif.getAttribute(ExifInterface.TAG_DATETIME));
            Log.e(LOG_ID, "DATETIME_DIGITIZED: " + exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED));
            Log.e(LOG_ID, "DATETIME_ORIGINAL: " + exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL));
            Double la1, la2, lo1, lo2;
            float p;
            la2 = 64.15633921;
            lo2 = -140.55884988;
            la1 = 64.0285107;
            lo1 = -139.44079987;
            p = (float) 0.5;
            extrapolatePoint epoint = new extrapolatePoint(la1, lo1, la2, lo2, p);
            Log.e(LOG_ID, "New Lat: " + epoint.latitude() + "New Lon: " + epoint.longitude());
            mfile.close();

        } catch (Exception e) {
            // if any I/O error occurs
            e.printStackTrace();

        }
    }

}
