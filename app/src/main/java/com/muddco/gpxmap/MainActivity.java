package com.muddco.gpxmap;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.model.LatLng;
import com.muddco.gpxmap.databinding.ActivityMainBinding;

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


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    static final String TAG = "TEST123";
    private static final int RQS_OPEN_GPX = 1;
    private static final int RQS_OPEN_PHOTO_TREE = 2;
    GPXParser mParser = new GPXParser();
    TrackData tData = new TrackData();
    LocalDateTime startTrack, endTrack;
    ArrayList<Photo> pData = new ArrayList<>();
    static public Context appContext;
    private ActivityMainBinding binding;
    private int cameraOffset = -5;
    private int localOffset = -8;
    private Spinner cameraOffsetSpinner;
    private Spinner localOffsetSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ArrayAdapter<CharSequence> tzOffsetAdapter;

        super.onCreate(savedInstanceState);

        // Bind the view
        // Uses the new "View Binding" feature introduced in Android Studio 3.6
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        appContext = getApplicationContext();

        //Set button click handlers
        binding.loadGpxButton.setOnClickListener(v -> loadGpxClicked());
        binding.loadPhotosButton.setOnClickListener(v -> loadPhotosClicked());

        // Set up spinners
        cameraOffsetSpinner = binding.cameraOffsetSpinner;
        localOffsetSpinner = binding.localOffsetSpinner;
        tzOffsetAdapter = ArrayAdapter.createFromResource(this, R.array.timezone_offsets, android.R.layout.simple_spinner_item);
        tzOffsetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cameraOffsetSpinner.setAdapter(tzOffsetAdapter);
        localOffsetSpinner.setAdapter(tzOffsetAdapter);
        cameraOffsetSpinner.setOnItemSelectedListener(this);
        localOffsetSpinner.setOnItemSelectedListener(this);

        // Set initial values for Spinners
        cameraOffsetSpinner.setSelection(12 + cameraOffset);
        localOffsetSpinner.setSelection(12 + localOffset);

        // Load Map
        addFragment(new MapFragment(), false, "one");
    }

    private void loadGpxClicked() {
        // Prompt user to select gpx file

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(Intent.createChooser(intent, "Select a GPX file"), RQS_OPEN_GPX);
    }

    private void loadPhotosClicked() {
        /*
         * Get the photo files.
         *
         * Normallky we would use the ACTION_OPEN_DOCUMENT_TREE intent.
         * However, that does not allow us to accedd OneDrive and other providers.
         * Insteade, we use ACTION_OPEN_DOCUMENT with a flag to indicate that the
         * user can select multiple files.
         */
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        startActivityForResult(Intent.createChooser(intent, "Choose Photos"), RQS_OPEN_PHOTO_TREE);
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
                // Process GPX file
                Uri gpxUri = data.getData();
                if (gpxUri != null) {
                    try {
                        InputStream inputStream = getBaseContext().getContentResolver().openInputStream(gpxUri);
                        loadGPXFile(inputStream);
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, "File not found: " + gpxUri.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(this, "URI is null!", Toast.LENGTH_LONG).show();

            } else if (requestCode == RQS_OPEN_PHOTO_TREE) {
                // We actually do not use OPEN_TREE because it will not woirk with OneDrive
                // Instead, we do an OPEN_DOCUMENT with a flag set that allows
                // the user to select multiple files/

                ClipData clipData = data.getClipData();
                Uri pUri = data.getData();
                if (clipData == null && pUri == null) {  // Don't know if this can actually happen...
                    Toast.makeText(this, "No jpeg files selected", Toast.LENGTH_LONG).show();
                } else if (clipData != null) {  // User selected multiple files
                    pData.clear();
                    int numPhotos = clipData.getItemCount();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        Photo photo = new Photo(getFileName(uri));
                        photo.setUri(uri);
                        pData.add(photo);
                    }
                } else { // User only selected one file
                    Photo photo = new Photo(getFileName(pUri));
                    photo.setUri(pUri);
                    pData.add(photo);
                }
                int pdataCount = pData.size();
                if (pData.size() == 0)  // I don't think this could actuallyt happen, but lets be safe...
                    Toast.makeText(this, "No jpeg files found", Toast.LENGTH_LONG).show();
                else {
                    Photo photo;
                    int pCount = 0;
                    Iterator itr = pData.iterator();
                    while (itr.hasNext()) {
                        photo = (Photo) itr.next();
                        LocalDateTime pDate = getPhotoDate(photo.getUri());
                        if (pDate == null)
                            itr.remove();
                        else {
                            LatLng pPos = findPhotoOnTrack(pDate);
                            if (pPos == null)
                                itr.remove();
                            else {
                                pCount++;
                                binding.numPhotos.setText(String.valueOf(pCount));
                                photo.setDate(pDate);
                                photo.setPosition(pPos);
                            }
                        }
                    }
                    Toast.makeText(this, pData.size() + " photos found", Toast.LENGTH_LONG).show();
                    if (pData.size() > 0)
                        MapFragment.displayPhotos(pData);
                }
            }
            int qqww = 345;
        }
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
        int numPoints = 0;

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
                        numPoints++;
                    }
                }
            }
            // Display the track on the map
            MapFragment.displayTrack(tData);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM dd yyyy hh:mm a");

            String sString = "invalid";
            String eString = "invalid";
            try {
                sString = startTrack.plusHours(localOffset).format(formatter);
                eString = endTrack.plusHours(localOffset).format(formatter);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            binding.trackStartTime.setText(sString);
            binding.trackEndTime.setText(eString);
            binding.trackNumPoints.setText(String.valueOf(numPoints));

        } else {
            Toast.makeText(this, "GPX parse failed", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error parsing gpx track!");
        }
    }

    //
    // Return the Lattitude and Longitude of where a photo was taken,
    // based on the time it was taken.
    //
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
            return null;  // Photo's date was before or after the start of the GPS track

        ArrayList<TrackPoint> points = tData.getTrackPoints();
        for (TrackPoint trackPoint : points) {
            pointdt = trackPoint.getTime();
            if (photodt.isAfter(pointdt)) {
                // The photo was taken AFTER this point
                previousLat = trackPoint.getLatitude();
                previousLon = trackPoint.getLongitude();
                previousTime = trackPoint.getTime();
            } else {
                // The photo was taken before or at the same time as this point
                try {
                    // Convert date/times to Milliseconds
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
                    // Photo was taken at a track point location
                    tagLat = trackPoint.getLatitude();
                    tagLon = trackPoint.getLongitude();

                }
                break;
            }
        }
        return (new LatLng(tagLat, tagLon));
    }

    /*
     * Returns a photo's creation date from it's EXIF data
     */
    private LocalDateTime getPhotoDate(Uri pUri) {
        InputStream mfile;
        LocalDateTime ret = null;
        String dtStr;

        try {
            InputStream inputStream = getBaseContext().getContentResolver().openInputStream(pUri);
            if (inputStream != null) {
                ExifInterface exif = new ExifInterface(inputStream);
                dtStr = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
                if (dtStr != null) {
                    // Convert string date into LocalDateTime, then adjust for timezone offset
                    // Timezone offset is the difference, in hours, between the timezone that the tracking
                    //     data was taken in and the timezone that the camera is set to.
                    ret = LocalDateTime.parse(dtStr, DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")).plusHours(cameraOffset * -1);
                    //String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                    //String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                }
                inputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Photo file not found: " + e.getLocalizedMessage());
        }

        return (ret);

    }

    //
    // Get the file name associated with a URI.
    //
    public String getFileName(Uri uri) {
        String result = null;
        int cut;

        String scheme = uri.getScheme();
        if (scheme != null && scheme.equals("content")) {
            //
            // query the URI provider for the file name
            //
            try (Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result != null)
            return result;

        result = uri.getPath();
        try {
            assert result != null;
            cut = result.lastIndexOf('/');
        } catch (NullPointerException e) {
            cut = -1;
        }
        if (cut != -1)
            result = result.substring(cut + 1);

        return result;
    }

    //
    // Callback for when a Spinner value has been selected
    //
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getId() == cameraOffsetSpinner.getId())
            cameraOffset = (int) id - 12;
        else if (parent.getId() == localOffsetSpinner.getId())
            localOffset = (int) id - 12;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Required by the Spinners
    }
}




