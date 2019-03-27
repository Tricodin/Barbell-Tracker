package com.example.opencvproject.detector;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;

public interface Detector {
    public abstract void apply (final Mat src, final Mat dst);

    public abstract ArrayList<Point> getPointList();

    public abstract boolean setTracking(boolean tracking);
}
