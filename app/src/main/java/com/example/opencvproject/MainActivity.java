package com.example.opencvproject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.math3.analysis.function.Pow;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.opencvproject.detector.DatabaseHelper;
import com.example.opencvproject.detector.Detector;
import com.example.opencvproject.detector.ReferenceDetector;

public class MainActivity extends Activity implements CvCameraViewListener2 //OnTouchListener,
{
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    private Detector[]           detector;
    private DatabaseHelper       mDatabaseHelper;
    private Uri                  imageURI;

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean takePhoto;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                    final Detector markerDetector;
                    try {
                        markerDetector = new ReferenceDetector(
                                MainActivity.this,
                                R.drawable.marker);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to load drawable: " +
                                "marker");
                        e.printStackTrace();
                        break;
                    }

                    detector = new Detector[] {markerDetector};

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mDatabaseHelper = new DatabaseHelper(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        if (detector != null) {
            detector[0].apply(
                    mRgba, mRgba);
        }

        if (takePhoto){
            takePhoto = false;
            takePhoto(mRgba);
        }

        return mRgba;
    }

    public void onButtonClick (View v){
        takePhoto = true;
        //logData();


    }

    private void takePhoto(final Mat rgba) {
// Determine the path and metadata for the photo.
        final long currentTimeMillis = System.currentTimeMillis();
        final String appName = getString(R.string.app_name);
        final String galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        final String albumPath = galleryPath + "/" + appName;
        final String photoPath = albumPath + "/" +
                currentTimeMillis + ".png";
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, photoPath);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.TITLE, appName);
        values.put(MediaStore.Images.Media.DESCRIPTION, appName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, currentTimeMillis);



    // Ensure that the album directory exists.
        File album = new File(albumPath);
        if (!album.isDirectory() && !album.mkdirs()) {
            Log.e(TAG, "Failed to create album directory at " +
                    albumPath);
            onTakePhotoFailed();
            return;
        }
        // Try to create the photo.
        Imgproc.cvtColor(rgba, mRgba, Imgproc.COLOR_RGBA2BGR, 3);
        if (!Imgcodecs.imwrite(photoPath, rgba)) {
            Log.e(TAG, "Failed to save photo to " + photoPath);
            onTakePhotoFailed();
        }
        Log.d(TAG, "Photo saved successfully to " + photoPath);
        // Try to insert the photo into the MediaStore.
        Uri uri;
        try {
            uri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            imageURI = uri;
            logData();

        } catch (final Exception e) {
            Log.e(TAG, "Failed to insert photo into MediaStore");
            e.printStackTrace();
            // Since the insertion failed, delete the photo.
            File photo = new File(photoPath);

            if (!photo.delete()) {
                Log.e(TAG, "Failed to delete non-inserted photo");
            }
            onTakePhotoFailed();
            return;
        }
    }
    private void onTakePhotoFailed() {
    // Show an error message.
        final String errorMessage ="Photo failed";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logData()
    {
        double Variance = calculateVariance(detector);
        double STDDev = java.lang.Math.sqrt(Variance);
        int devInt = (int)STDDev;

        String DevString = Integer.toString(devInt);

        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

        String URIString = imageURI.toString();

        mDatabaseHelper.addData(date, DevString, URIString);
    }


    private double calculateVariance(Detector[] rDetector)
    {
        ArrayList<Point> points = rDetector[0].getPointList();
        double[] xPoints =  new double[points.size()];
        double sum = 0;
        double mean;
        double currVar = 0;

        for(int i = 0; i < points.size(); i++ )
        {
            xPoints[i] = points.get(i).x;
            sum = sum + xPoints[i];
        }

        mean = sum / (points.size());

        for(int j = 0; j<xPoints.length;j++)
        {
            currVar = currVar + ((xPoints[j] - mean) * (xPoints[j] - mean));
        }

        return currVar / (xPoints.length - 1);


    }
}

