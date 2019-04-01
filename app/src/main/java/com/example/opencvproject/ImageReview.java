package com.example.opencvproject;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageReview extends AppCompatActivity {

    public static final String PHOTO_MIME_TYPE = "image/png";
    public static final String EXTRA_PHOTO_URI = "com.example.opencvproject.ImageReview.extra.PHOTO_URI";
    public static final String EXTRA_PHOTO_DATA_PATH = "com.example.opencvproject.ImageReview.extra.PHOTO_DATA_PATH";

    private Uri mUri;
    private String mDataPath;

    //Displays imageURI that is passed in
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        mUri = intent.getParcelableExtra(EXTRA_PHOTO_URI);
        //        mDataPath = intent.getStringExtra(EXTRA_PHOTO_DATA_PATH);
        final ImageView imageView = new ImageView(this);
        imageView.setImageURI(mUri);
        setContentView(imageView);
    }
}
