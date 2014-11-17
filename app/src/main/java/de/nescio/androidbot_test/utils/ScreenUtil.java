package de.nescio.androidbot_test.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Created by Julia on 17.11.2014.
 */
public class ScreenUtil {
    public static String LANDSCAPE_NORMAL = "landscape_normal";
    public static String PORTRAIT_NORMAL = "portrait_normal";
    public static String LANDSCAPE_INVERTED = "landscape_inverted";
    public static String PORTRAIT_INVERTED = "portrait_inverted";

    public static String getScreenOrientation(WindowManager windowManager) {
        int rotation =windowManager.getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        String mode = PORTRAIT_NORMAL;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    mode = PORTRAIT_NORMAL;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    mode = LANDSCAPE_NORMAL;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    mode = PORTRAIT_INVERTED;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    mode = LANDSCAPE_INVERTED;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    mode = PORTRAIT_NORMAL;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
//        else {
//            switch (rotation) {
//                case Surface.ROTATION_0:
//                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
//                    break;
//                case Surface.ROTATION_90:
//                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//                    break;
//                case Surface.ROTATION_180:
//                    orientation =
//                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
//                    break;
//                case Surface.ROTATION_270:
//                    orientation =
//                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
//                    break;
//                default:
//                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
//                    break;
//            }
//        }

        return mode;
    }
}
