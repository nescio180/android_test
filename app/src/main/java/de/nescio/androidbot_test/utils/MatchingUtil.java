package de.nescio.androidbot_test.utils;

/**
 * Created by Julia on 23.11.2014.
 */
public class MatchingUtil {
    public void match(String inFile, String outFile, String templateFile, int match_method) {
//        Mat img = Highgui.imread(inFile);
//        Mat templ = Highgui.imread(templateFile);
//
//        // / Create the result matrix
//        int result_cols = img.cols() - templ.cols() + 1;
//        int result_rows = img.rows() - templ.rows() + 1;
//        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
//
//        // / Do the Matching and Normalize
//        Imgproc.matchTemplate(img, templ, result, match_method);
//        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
//
//        // / Localizing the best match with minMaxLoc
//        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
//
//        org.opencv.core.Point matchLoc;
//        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
//            matchLoc = mmr.minLoc;
//        } else {
//            matchLoc = mmr.maxLoc;
//        }
//
//        // / Show me what you got
//        Core.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(),
//                matchLoc.y + templ.rows()), new Scalar(0, 255, 0));
//
//        // Save the visualized detection.
//        System.out.println("Writing " + outFile);
//        Highgui.imwrite(outFile, img);
    }

}
