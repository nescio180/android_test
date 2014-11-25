package de.nescio.androidbot_test.utils;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

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
        Core.normalize(source, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        org.opencv.core.Point matchLoc;
        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
        } else {
            matchLoc = mmr.maxLoc;
        }

        android.util.Log.d("",""+ mmr.maxVal + ", " + mmr.minVal + ", " + mmr.minVal + ", " + mmr.minLoc);

        // / Show me what you got
        Core.rectangle(source, matchLoc, new Point(matchLoc.x + template.cols(), matchLoc.y + template.rows()),
                new Scalar(0, 255, 0), -1);

        boolean write = Highgui.imwrite(outFile, source);

        if (hasChanged) {
            match(outFile, outFile, templateFile, match_method);
        }
    }

    public static boolean matchFeature(String inFile, String templateFile) {
        OpenCVLoader.initDebug();

        FeatureDetector fd = FeatureDetector.create(FeatureDetector.FAST);
        final MatOfKeyPoint keyPointsLarge = new MatOfKeyPoint();
        final MatOfKeyPoint keyPointsSmall = new MatOfKeyPoint();

        Mat largeImage = Highgui.imread(inFile);
        Mat smallImage = Highgui.imread(templateFile);

        fd.detect(largeImage, keyPointsLarge);
        fd.detect(smallImage, keyPointsSmall);

        System.out.println("keyPoints.size() : " + keyPointsLarge.size());
        System.out.println("keyPoints2.size() : " + keyPointsSmall.size());

        Mat descriptorsLarge = new Mat();
        Mat descriptorsSmall = new Mat();

        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
        extractor.compute(largeImage, keyPointsLarge, descriptorsLarge);
        extractor.compute(smallImage, keyPointsSmall, descriptorsSmall);

        System.out.println("descriptorsA.size() : " + descriptorsLarge.size());
        System.out.println("descriptorsB.size() : " + descriptorsSmall.size());

        MatOfDMatch matches = new MatOfDMatch();

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
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
                //System.out.println(String.format(i + " best match added : %s", dist));
            }
        }

        matchesFiltered.fromList(bestMatches);

        System.out.println("matchesFiltered.size() : " + matchesFiltered.size());

        if (matchesFiltered.rows() >= 1) {
            System.out.println("match found");
            int x = bestMatches.get(0).queryIdx;
            int y = bestMatches.get(0).trainIdx;
            System.out.println("match found : " + "x: " + x + ", y:" + y);
            return true;
        } else {
            return false;
        }
    }
}