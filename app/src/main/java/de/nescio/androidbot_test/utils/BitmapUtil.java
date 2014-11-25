package de.nescio.androidbot_test.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.DataOutputStream;

/**
 * Created by Wohnzimmer on 16.11.2014.
 */
public class BitmapUtil {
    private static Bitmap bitmapFromSD;

    public static boolean takeScreenShot() {
        Process shell;
        try {
            shell = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(shell.getOutputStream());

            os.writeBytes("/system/bin/screencap -p /sdcard/screen.png\n");
            os.writeBytes("exit\n");
            os.flush();
            os.close();

            shell.waitFor();
        } catch (Exception e) {
            return false;
        }

        shell.destroy();

        return true;
    }
}
