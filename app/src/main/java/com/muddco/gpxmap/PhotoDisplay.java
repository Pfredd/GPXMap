package com.muddco.gpxmap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PhotoDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_display);

        // Fetch the Uri parameter, sent by the caller
        Intent myIntent = getIntent();
        Uri uri = Uri.parse(myIntent.getStringExtra("uri"));

        // Open the photo file
        InputStream inputStream = null;
        try {
            inputStream = getBaseContext().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Create a bitmap from the file
        Bitmap bm = BitmapFactory.decodeStream(inputStream);
        try {
            assert inputStream != null;
            inputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        // Set the imageView's bitmap
        ImageView imageView = findViewById(R.id.img);
        imageView.setImageBitmap(bm);

        // Handle the close button click by exiting
        ImageButton closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            // Close this Activity
            finish();
        });
    }
}
