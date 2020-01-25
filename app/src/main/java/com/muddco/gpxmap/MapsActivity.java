package com.muddco.gpxmap;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

// For androidx.exifinterface  - need to add dependancy for
// androidx.exifinterface:exifinterface:1.1.0


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    static final String TAG = "TEST123"; // GPXParserSampleActivity.class.getSimpleName();

    GPXParser mParser = new GPXParser();

    TrackData tData = new TrackData();
    String gpxFileName = "20190627.gpx";
    String photoFileName = "images/DSC01042.JPG";


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

        long tzOffset = 5;

        polyLine.geodesic(false);   // Not needed on our small scale map
        polyLine.width(12);
        polyLine.color(Color.RED);

        Gpx parsedGpx = null;

        /*
         * Open and parse gpx file
         */
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

        /*
         * Add track to the map
         */
        if (parsedGpx != null) {
            List<Track> tracks = parsedGpx.getTracks();
            for (int i = 0; i < tracks.size(); i++) {
                Track track = tracks.get(i);
                List<TrackSegment> segments = track.getTrackSegments();
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

        position = findPhotoOnTrack(tData, photoFileName, tzOffset);
        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Hello world\n"));

    }

    private LatLng findPhotoOnTrack(TrackData tData, String photoFilename, long tzOffset) {
        Double previousLat = 0.0;
        Double previousLon = 0.0;
        LocalDateTime previousTime = null;
        LocalDateTime pointdt;
        long millisToPhoto = 0;
        long millisToEndpoint = 0;
        Double tagLat = 0.0;
        Double tagLon = 0.0;

        ArrayList<TrackPoint> points = tData.getTrackPoints();
        String photoDate = getPhotoDate(photoFileName);
        LocalDateTime photodt = LocalDateTime.parse(photoDate, DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")).plusHours(tzOffset);
        for (TrackPoint trackPoint : points) {
            pointdt = trackPoint.getTime();
            if (!photodt.isBefore(pointdt) && !photodt.isEqual(pointdt)) {
                Boolean test = photodt.isEqual(pointdt);
                previousLat = trackPoint.getLatitude();
                previousLon = trackPoint.getLongitude();
                previousTime = trackPoint.getTime();
            } else {
                try {
                    millisToPhoto = java.time.Duration.between(previousTime, photodt).toMillis();
                    millisToEndpoint = java.time.Duration.between(previousTime, pointdt).toMillis();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                if (millisToEndpoint != millisToPhoto) {
                    // Photo was taken between track data points. So we have to extrapolate the position
                    float pct = (float) millisToPhoto / (float) millisToEndpoint;
                    extrapolatePoint midPoint = new extrapolatePoint(previousLat, previousLon, trackPoint.getLatitude(), trackPoint.getLongitude(), pct);
                    tagLat = midPoint.latitude();
                    tagLon = midPoint.longitude();
                } else {
                    // Photo was taken at a track point locatrion
                    tagLat = trackPoint.getLatitude();
                    tagLon = trackPoint.getLongitude();

                }
                break;
            }
        }
        return (new LatLng(tagLat, tagLon));
    }

    /*
     * Returns a photo's creation dare from it's EXIF data
     */
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

    private static void setGPS(String filePath, Double latitude, Double longitude) {

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
            mfile = getAssets().open("images/DSC01638.JPG");
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
