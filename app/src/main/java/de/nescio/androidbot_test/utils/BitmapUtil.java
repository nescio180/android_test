package de.nescio.androidbot_test.utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Wohnzimmer on 16.11.2014.
 */
public class BitmapUtil {
    private static BitmapUtil mInstance;
    private Process mShell;

    private BitmapUtil() {
        try {
            mShell = Runtime.getRuntime().exec("su", null,null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BitmapUtil getInstance() {
        if (mInstance == null) {
            mInstance = new BitmapUtil();
        }
        return mInstance;
    }

    public void takeScreenShot() {
        OutputStream os = mShell.getOutputStream();
        try {
            os.write(("/system/bin/screencap -p " + "/sdcard/bot/screen.png").getBytes("ASCII"));
            os.flush();
            os.close();
            mShell.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
