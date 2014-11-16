package de.nescio.androidbot_test.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import de.nescio.androidbot_test.service.OverlayShowingService;
import de.nescio.androidbot_test.utils.BitmapUtil;
import de.nescio.androidbottest.R;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent svc = new Intent(this, OverlayShowingService.class);
        startService(svc);
        finish();
    }
}