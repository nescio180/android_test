package de.nescio.androidbot_test.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import de.nescio.androidbot_test.service.OverlayShowingService;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent svc = new Intent(this, OverlayShowingService.class);
        startService(svc);
        finish();
    }
}