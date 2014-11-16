package de.nescio.androidbot_test.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Wohnzimmer on 16.11.2014.
 */
public class BitmapUtil {

    private static Bitmap bitmapFromSD;

    public static boolean takeScreenShot() {
        Process shell = null;
        try {
            shell = Runtime.getRuntime().exec("su", null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream os = shell.getOutputStream();
        try {
            os.write(("/system/bin/screencap -p " + "/sdcard/screen.png").getBytes("ASCII"));
            os.flush();
            os.close();
            shell.waitFor();
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }

        shell.destroy();
        return true;
    }

    public static Bitmap getBitmapFromSD() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmapFromSD = BitmapFactory.decodeFile("/sdcard/screen.png", options);
        return bitmapFromSD;
    }

    public static Bitmap getScreenBitmap() {
        boolean success = takeScreenShot();
        return getBitmapFromSD();
    }

    public static int[] getBitmapPixel(Bitmap b) {
        int[] pixel = new int[b.getHeight() * b.getWidth()];
        b.getPixels(pixel, 0, b.getWidth(), 0, 0,
                b.getWidth(), b.getHeight());
        return pixel;
    }
}
