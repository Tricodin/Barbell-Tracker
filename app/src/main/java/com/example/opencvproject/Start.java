package com.example.opencvproject;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Start extends AppCompatActivity {
    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final int REQUEST_PICK_VIDEO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }
    public void launchTracker(View v)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void buttonOnVideoClick(View v)
    {
        pickVideo();
    }


    void pickVideo() {
        Intent pickVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickVideoIntent.setType("video/*");
        startActivityForResult(pickVideoIntent, REQUEST_PICK_VIDEO);
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    public void recordVideoClick(View v)
    {
        dispatchTakeVideoIntent();
    }
}


