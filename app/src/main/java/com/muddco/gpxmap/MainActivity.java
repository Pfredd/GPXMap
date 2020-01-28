package com.muddco.gpxmap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;


public class MainActivity extends AppCompatActivity {

    private Button btn1, btn2;
    static final String TAG = "TEST123"; // GPXParserSampleActivity.class.getSimpleName();

    GPXParser mParser = new GPXParser();

    TrackData tData = new TrackData();
    LocalDateTime startTrack, endTrack;
    ArrayList<Photo> pData = new ArrayList<Photo>();
    String gpxFileName = "20190627.gpx";
    String photoFileName = "images/DSC01042.JPG";
    long tzOffset = 5;

    private static final int RQS_OPEN_GPX = 1;
    private static final int RQS_OPEN_PHOTO_TREE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/gpx+xml");

                startActivityForResult(intent, RQS_OPEN_GPX);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Choose a directory using the system's file picker.
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

                // Provide read access to files and sub-directories in the user-selected
                // directory.
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Optionally, specify a URI for the directory that should be opened in
                // the system file picker when it loads.
                //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);

                startActivityForResult(intent, RQS_OPEN_PHOTO_TREE);

//                getPhotos();
//                MapFragment.displayPhotos(pData);

            }
        });

        // Load the map
        addFragment(new MapFragment(), false, "one");
    }

    public void loadGPXFile(InputStream in) {
        Gpx parsedGpx = null;

        try {
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
                for (int j = 0; j < segments.size(); j++) {
                    TrackSegment segment = segments.get(j);
                    boolean firstPoint = true;
                    for (TrackPoint trackPoint : segment.getTrackPoints()) {
                        if (firstPoint) {
                            startTrack = trackPoint.getTime();
                            firstPoint = false;
                        } else
                            endTrack = trackPoint.getTime();
                        tData.addTrackPoint(trackPoint);
                    }
                }
            }
            // Display the track on the map
            MapFragment.displayTrack(tData);

        } else {
            Toast.makeText(this, "GPX parse failed", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error parsing gpx track!");
        }

    }

    public void getPhotos() {
        requestPhotos(pData);
        for (Photo photo : pData) {
            //String pdate = getPhotoDate(photo.getFname());
            //photo.setDate(LocalDateTime.parse(pdate, DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")).plusHours(tzOffset));
            // photo.setPosition(findPhotoOnTrack(photo.getFname(), photo.getDate()));
            int ww = 3;
        }

    }

    public void requestPhotos(ArrayList<Photo> pData) {
        pData.clear();
        Photo testPhoto = new Photo(photoFileName);
        pData.add(testPhoto);
    }

    private LatLng findPhotoOnTrack(LocalDateTime photodt) {
        Double previousLat = 0.0;
        Double previousLon = 0.0;
        LocalDateTime previousTime = null;
        LocalDateTime pointdt;
        long millisToPhoto = 0;
        long millisToEndpoint = 0;
        Double tagLat = 0.0;
        Double tagLon = 0.0;

        if (photodt.isBefore(startTrack) || photodt.isAfter(endTrack))
            return null;

        ArrayList<TrackPoint> points = tData.getTrackPoints();
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
    private LocalDateTime getPhotoDate(Uri pUri) {
        InputStream mfile;
        LocalDateTime ret = null;
        String dtStr;

        try {
            InputStream inputStream = getBaseContext().getContentResolver().openInputStream(pUri);
            ExifInterface exif = new ExifInterface(inputStream);
            dtStr = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
            if (dtStr != null)
                ret = LocalDateTime.parse(dtStr, DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")).plusHours(tzOffset);
            //String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            //String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Photo file not found: " + e.getLocalizedMessage());
        }

        return (ret);

    }

    public void addFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.replace(R.id.container_frame_back, fragment, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {


            if (requestCode == RQS_OPEN_GPX) {

                Uri gpxUri = data.getData();

                //Open the stream and process the file
                try {
                    InputStream inputStream = getBaseContext().getContentResolver().openInputStream(gpxUri);
                    loadGPXFile(inputStream);
                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "File not found: " + gpxUri.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else if (requestCode == RQS_OPEN_PHOTO_TREE) {
                Uri uriTree = data.getData();
                DocumentFile documentFile = DocumentFile.fromTreeUri(this, uriTree);
                pData.clear();
                if (documentFile.listFiles() != null) {
                    for (DocumentFile file : documentFile.listFiles()) {
                        if (!file.isDirectory() && file.getType().equals("image/jpeg")) {
                            Photo photo = new Photo(file.getName());
                            photo.setUri(file.getUri());
                            pData.add(photo);
                        }
                    }
                }
            }
            if (pData.size() == 0)
                Toast.makeText(this, "No jpeg files found", Toast.LENGTH_LONG).show();
            else {
                // Add a tag to the map for each file who's date/time is on the track's timeline
                Iterator itr = pData.iterator();
                Photo photo;
                while (itr.hasNext()) {
                    photo = (Photo) itr.next();
                    LocalDateTime pDate = getPhotoDate(photo.getUri());
                    LatLng pPos = findPhotoOnTrack(pDate);
                    if (pDate == null || pPos == null)
                        itr.remove();
                    else {
                        photo.setDate(pDate);
                        photo.setPosition(pPos);
                    }

                }
                int eww = pData.size();
                Toast.makeText(this, "Size: " + eww, Toast.LENGTH_LONG).show();
                MapFragment.displayPhotos(pData);
            }
        }
    }

}


