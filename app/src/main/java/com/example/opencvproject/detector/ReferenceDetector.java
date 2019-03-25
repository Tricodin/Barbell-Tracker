package com.example.opencvproject.detector;

import android.content.Context;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReferenceDetector implements Detector {
    private final Mat mReferenceImage;
    private final MatOfKeyPoint mReferenceKeypoints =
            new MatOfKeyPoint();
    private final Mat mReferenceDescriptors = new Mat();
    // CVType defines the color depth, number of channels, and
// channel layout in the image.
    private final Mat mReferenceCorners =
            new Mat(4, 1, CvType.CV_32FC2);
    private final Mat mReferenceCenter = new Mat(1, 1,
            CvType.CV_32FC2);
    private final MatOfKeyPoint mSceneKeypoints =
            new MatOfKeyPoint();
    private final Mat mSceneDescriptors = new Mat();
    private final Mat mCandidateSceneCorners =
            new Mat(4, 1, CvType.CV_32FC2);
    private final Mat mSceneCorners = new Mat(4, 1,
            CvType.CV_32FC2);
    private final Mat mSceneCenter = new Mat(1, 1,
            CvType.CV_32FC2);
    private final MatOfPoint mIntSceneCorners = new MatOfPoint();
    private final Mat mGraySrc = new Mat();
    private final MatOfDMatch mMatches = new MatOfDMatch();
    private final ORB orb = ORB.create();

    private final DescriptorMatcher mDescriptorMatcher =
            DescriptorMatcher.create(
                    DescriptorMatcher.BRUTEFORCE_HAMMING);
    private final Scalar mLineColor = new Scalar(0, 255, 0);
    private final Scalar trackLineColor = new Scalar(255,0,0);

    private ArrayList<Point> listOfPoints;

    public ReferenceDetector(final Context context,
                             final int referenceImageResourceID) throws IOException {
        mReferenceImage = Utils.loadResource(context,
                referenceImageResourceID,
                Imgcodecs.CV_LOAD_IMAGE_COLOR);
        final Mat referenceImageGray = new Mat();
        Imgproc.cvtColor(mReferenceImage, referenceImageGray,
                Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mReferenceImage, mReferenceImage,
                Imgproc.COLOR_BGR2RGBA);
        mReferenceCorners.put(0, 0,
                new double[] {0.0, 0.0});
        mReferenceCorners.put(1, 0,
                new double[] {referenceImageGray.cols(), 0.0});
        mReferenceCorners.put(2, 0,
                new double[] {referenceImageGray.cols(),
                        referenceImageGray.rows()});
        mReferenceCorners.put(3, 0,
                new double[] {0.0, referenceImageGray.rows()});
        mReferenceCenter.put(0, 0,
                new double[] {referenceImageGray.cols()/2, referenceImageGray.rows()/2});
        orb.detect(referenceImageGray,
                mReferenceKeypoints);
        orb.compute(referenceImageGray,
                mReferenceKeypoints, mReferenceDescriptors);
        listOfPoints = new ArrayList<Point>();
    }

    @Override
    public void apply(final Mat src, final Mat dst) {
        Imgproc.cvtColor(src, mGraySrc, Imgproc.COLOR_RGBA2GRAY);
        orb.detect(mGraySrc, mSceneKeypoints);
        orb.compute(mGraySrc, mSceneKeypoints,
                mSceneDescriptors);
        mDescriptorMatcher.match(mSceneDescriptors,
                mReferenceDescriptors, mMatches);
        findMarkerCorners();
        draw(src, dst);
    }

    private void findMarkerCorners() {
        List<DMatch> matchesList = mMatches.toList();
        if (matchesList.size() < 4) {
// There are too few matches to find the homography.
            return;
        }
        List<KeyPoint> referenceKeypointsList =
                mReferenceKeypoints.toList();
        List<KeyPoint> sceneKeypointsList =
                mSceneKeypoints.toList();
// Calculate the max and min distances between keypoints.
        double maxDist = 0.0;
        double minDist = Double.MAX_VALUE;
        for(DMatch match : matchesList) {
            double dist = match.distance;
            if (dist < minDist) {
                minDist = dist;
            }
            if (dist > maxDist) {
                maxDist = dist;
            }
        }
// The thresholds for minDist are chosen subjectively
// based on testing. The unit is not related to pixel
// distances; it is related to the number of failed tests
// for similarity between the matched descriptors.
        if (minDist > 40.0) {
// The target is completely lost.
// Discard any previously found corners.
            mSceneCorners.create(0, 0, mSceneCorners.type());
            return;
        } else if (minDist > 25.0) {
// The target is lost but maybe it is still close.
// Keep any previously found corners.
            return;
        }

        // Identify "good" keypoints based on match distance.
        ArrayList<Point> goodReferencePointsList =
                new ArrayList<Point>();
        ArrayList<Point> goodScenePointsList =
                new ArrayList<Point>();
        double maxGoodMatchDist = 1.75 * minDist;
        for(DMatch match : matchesList) {
            if (match.distance < maxGoodMatchDist) {
                goodReferencePointsList.add(
                        referenceKeypointsList.get(match.trainIdx).pt);
                goodScenePointsList.add(
                        sceneKeypointsList.get(match.queryIdx).pt);
            }
        }
        if (goodReferencePointsList.size() < 4 ||
                goodScenePointsList.size() < 4) {
// There are too few good points to find the homography.
            return;
        }
        MatOfPoint2f goodReferencePoints = new MatOfPoint2f();
        goodReferencePoints.fromList(goodReferencePointsList);
        MatOfPoint2f goodScenePoints = new MatOfPoint2f();
        goodScenePoints.fromList(goodScenePointsList);
        Mat homography = Calib3d.findHomography(
                goodReferencePoints, goodScenePoints);
        Core.perspectiveTransform(mReferenceCorners,
                mCandidateSceneCorners, homography);
        mCandidateSceneCorners.convertTo(mIntSceneCorners,
                CvType.CV_32S);
        if (Imgproc.isContourConvex(mIntSceneCorners)) {
            mCandidateSceneCorners.copyTo(mSceneCorners);
            Core.perspectiveTransform(mReferenceCenter,
                    mSceneCenter, homography);
            listOfPoints.add(new Point (mSceneCenter.get(0,0)));
        }
    }

    protected void draw(Mat src, Mat dst) {
        if (dst != src) {
            src.copyTo(dst);
        }
        if (mSceneCorners.height() < 4) {
            return;
        }
// Outline the found target in green.
        Imgproc.line(dst, new Point(mSceneCorners.get(0, 0)),
                new Point(mSceneCorners.get(1, 0)), mLineColor, 4);
        Imgproc.line(dst, new Point(mSceneCorners.get(1, 0)),
                new Point(mSceneCorners.get(2, 0)), mLineColor, 4);
        Imgproc.line(dst, new Point(mSceneCorners.get(2, 0)),
                new Point(mSceneCorners.get(3, 0)), mLineColor, 4);
        Imgproc.line(dst, new Point(mSceneCorners.get(3,0)),
                new Point(mSceneCorners.get(0, 0)), mLineColor, 4);

        //Places a circle around the "centre"
//        if (listOfPoints.size() > 0)
//            Imgproc.circle(dst,listOfPoints.get(listOfPoints.size()-1),5,trackLineColor,4);

        //Draws the center travel path
        for (int i = 1; i < listOfPoints.size(); i++){
            Imgproc.line(dst, listOfPoints.get(i-1),listOfPoints.get(i),trackLineColor,4);
        }
    }
}


