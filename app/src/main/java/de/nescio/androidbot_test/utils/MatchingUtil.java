package de.nescio.androidbot_test.utils;

import android.os.Environment;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julia on 23.11.2014.
 */
public class MatchingUtil {
    public static void match(String inFile, String outFile, String templateFile, int match_method) {
        Mat source = Highgui.imread(inFile);
        Mat template = Highgui.imread(templateFile);

        int result_cols = source.cols() - template.cols() + 1;
        int result_rows = source.rows() - template.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
        boolean hasChanged = false;

        Imgproc.matchTemplate(source, template, result, match_method);
        //Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Imgproc.threshold(result, result, 0.1, 1, Imgproc.THRESH_TOZERO);

        while(true)
        {
            double minval, maxval, threshold = 0.6;
            Point minloc, maxloc;
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
            minloc = mmr.minLoc;
            maxloc = mmr.maxLoc;
            minval = mmr.minVal;
            maxval = mmr.maxVal;

            android.util.Log.d("", "TESTTEST " + maxval + ", " + mmr.minVal + ", " + mmr.maxLoc + ", " + mmr.minLoc);
            if(maxval >= threshold)
            {
                android.util.Log.d("", "TESTTEST2 " + maxval + ", " + mmr.minVal + ", " + mmr.maxLoc + ", " + mmr.minLoc);
                Core.rectangle(source, maxloc, new Point(maxloc.x + template.cols(), maxloc.y + template.rows()),
                        new Scalar(0, 255, 0), 2);

                Rect rect = new Rect();
                Mat mat = new Mat(source.rows() + 2, source.cols() + 2,
                        CvType.CV_8UC1);
                Imgproc.floodFill(source, mat, maxloc, Scalar.all(255), rect,
                        Scalar.all(0), Scalar.all(0), Imgproc.);

                boolean write = Highgui.imwrite(outFile, source);
//                Imgproc.floodFill(source, new Mat(), maxloc, new Scalar(0));
                break;
            }
            else
            {
                break;
            }
        }
//
//
//        // / Show me what you got
//        Core.rectangle(source, matchLoc, new Point(matchLoc.x + template.cols(), matchLoc.y + template.rows()),
//                new Scalar(0, 255, 0), -1);
//
//
//        if (hasChanged) {
//            match(outFile, outFile, templateFile, match_method);
//        }
    }

    public static boolean matchFeature(String inFile, String templateFile, File cacheDir) {
        FeatureDetector fast = FeatureDetector.create(FeatureDetector.FAST);

        String filename = Environment.getExternalStorageDirectory() + "fast_params.yml";
        System.out.println("sdcard : " +  Environment.getExternalStorageDirectory());
        File f = new File(filename);
        writeToFile(f, "%YAML:1.0\nimage: mIntermediateMat\nkeypoints: points\nthreshold : 300 \nnonmaxSupression : true\ntype : FastFeatureDetector::TYPE_9_16\n");
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
        extractor.read(filename);

        final MatOfKeyPoint keyPointsLarge = new MatOfKeyPoint();
        final MatOfKeyPoint keyPointsSmall = new MatOfKeyPoint();
        Mat largeImage = Highgui.imread(inFile);
        Mat smallImage = Highgui.imread(templateFile);

        fast.detect(largeImage, keyPointsLarge);
        fast.detect(smallImage, keyPointsSmall);

        System.out.println("keyPoints.size() : " + keyPointsLarge.size());
        System.out.println("keyPoints2.size() : " + keyPointsSmall.size());

        Mat descriptorsLarge = new Mat();
        Mat descriptorsSmall = new Mat();

        extractor.compute(largeImage, keyPointsLarge, descriptorsLarge);
        extractor.compute(smallImage, keyPointsSmall, descriptorsSmall);

        System.out.println("descriptorsA.size() : " + descriptorsLarge.size());
        System.out.println("descriptorsB.size() : " + descriptorsSmall.size());

        MatOfDMatch matches = new MatOfDMatch();

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        matcher.match(descriptorsLarge, descriptorsSmall, matches);

        System.out.println("matches.size() : " + matches.size());

        MatOfDMatch matchesFiltered = new MatOfDMatch();

        List<DMatch> matchesList = matches.toList();
        List<DMatch> bestMatches = new ArrayList<DMatch>();

        Double max_dist = 0.0;
        Double min_dist = 100.0;

        for (int i = 0; i < matchesList.size(); i++) {
            Double dist = (double) matchesList.get(i).distance;

            if (dist < min_dist && dist != 0) {
                min_dist = dist;
            }

            if (dist > max_dist) {
                max_dist = dist;
            }

        }

        System.out.println("max_dist : " + max_dist);
        System.out.println("min_dist : " + min_dist);

        if (min_dist > 50) {
            System.out.println("No match found");
            System.out.println("Just return ");
            return false;
        }

        double threshold = 3 * min_dist;
        double threshold2 = 2 * min_dist;

        if (threshold > 75) {
            threshold = 75;
        } else if (threshold2 >= max_dist) {
            threshold = min_dist * 1.1;
        } else if (threshold >= max_dist) {
            threshold = threshold2 * 1.4;
        }

        System.out.println("Threshold : " + threshold);

        for (int i = 0; i < matchesList.size(); i++) {
            Double dist = (double) matchesList.get(i).distance;

            if (dist < threshold) {
                bestMatches.add(matches.toList().get(i));
                System.out.println(String.format(i + " best match added : %s", dist));
            }
        }

        matchesFiltered.fromList(bestMatches);

        System.out.println("matchesFiltered.size() : " + matchesFiltered.size());

        if (matchesFiltered.rows() >= 1) {
//            for (int i = 0; i < matchesFiltered.rows(); i++) {
//                int x = bestMatches.get(i).queryIdx;
//                int y = bestMatches.get(i).trainIdx;
//
//                Point px = keyPointsLarge.toArray()[x].pt;
//                Point py = keyPointsSmall.toArray()[y].pt;
//
//                Core.rectangle(largeImage, px, new Point(px.x + 10, px.y + 10),
//                        new Scalar(0, 255, 0), -1);
//
//                System.out.println("match found : " + "x: " + px + ", y:" + py);
//            }
            Mat out = new Mat();
            MatOfByte mask = new MatOfByte();
            Features2d.drawMatches(largeImage, keyPointsLarge, smallImage, keyPointsSmall, matchesFiltered, out, new Scalar(0, 255, 0), new Scalar(255, 0, 0), mask, Features2d.NOT_DRAW_SINGLE_POINTS);
            boolean write = Highgui.imwrite("sdcard/out.png", out);
            return true;
        } else {
            return false;
        }
    }

    private static void writeToFile(File file, String data) {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(stream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}