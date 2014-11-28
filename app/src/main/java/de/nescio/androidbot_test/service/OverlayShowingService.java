package de.nescio.androidbot_test.service;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

import org.opencv.android.OpenCVLoader;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import de.nescio.androidbot_test.utils.BitmapUtil;
import de.nescio.androidbot_test.utils.MatchingUtil;
import de.nescio.androidbot_test.utils.Point;
import de.nescio.androidbottest.R;

/**
 * Created by Wohnzimmer on 15.11.2014.
 */
public class OverlayShowingService extends Service implements View.OnTouchListener, OnClickListener {
    private WindowManager mWindowManager;
    private Button mButtonToggle, mButtonPlay, mButtonExit, mButtonScan;
    private View topLeftView;
    private View mMainView;
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;
    private volatile ArrayList<Point> mClickList = new ArrayList<Point>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    public void init() {
        OpenCVLoader.initDebug();

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mMainView = inflater.inflate(R.layout.overlay_view, null);
        mMainView.setOnTouchListener(this);

        mButtonPlay = (Button) mMainView.findViewById(R.id.overlay_button_play);
        mButtonExit = (Button) mMainView.findViewById(R.id.overlay_button_exit);
        mButtonToggle = (Button) mMainView.findViewById(R.id.overlay_button_toggle);
        mButtonScan = (Button) mMainView.findViewById(R.id.overlay_button_scan);

        mButtonPlay.setOnClickListener(this);
        mButtonExit.setOnClickListener(this);
        mButtonToggle.setOnClickListener(this);
        mButtonScan.setOnClickListener(this);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        mWindowManager.addView(mMainView, params);

        topLeftView = new View(this);
        WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        topLeftParams.gravity = Gravity.LEFT | Gravity.TOP;
        topLeftParams.x = 0;
        topLeftParams.y = 0;
        topLeftParams.width = 0;
        topLeftParams.height = 0;
        mWindowManager.addView(topLeftView, topLeftParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMainView != null) {
            mWindowManager.removeView(mMainView);
            mWindowManager.removeView(topLeftView);
            mMainView = null;
            topLeftView = null;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();
            moving = false;

            int[] location = new int[2];
            mMainView.getLocationOnScreen(location);

            originalXPos = location[0];
            originalYPos = location[1];
            offsetX = originalXPos - x;
            offsetY = originalYPos - y;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getRawX();
            float y = event.getRawY();
            int newX = (int) (offsetX + x);
            int newY = (int) (offsetY + y);

            if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
                return false;
            }

            int[] topLeftLocationOnScreen = new int[2];
            topLeftView.getLocationOnScreen(topLeftLocationOnScreen);

            WindowManager.LayoutParams params = (LayoutParams) mMainView.getLayoutParams();
            params.x = newX - (topLeftLocationOnScreen[0]);
            params.y = newY - (topLeftLocationOnScreen[1]);

            mWindowManager.updateViewLayout(mMainView, params);
            moving = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (moving) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View _view) {
        if (_view == mButtonToggle) {
            //MatchingUtil.matchFeature("/sdcard/screen.png", "/sdcard/coc_elixir_icon.png", getCacheDir());
            MatchingUtil.match("/sdcard/screen.png", "/sdcard/out.png", "/sdcard/coc_elixir_icon_small.png", Imgproc.TM_SQDIFF_NORMED);
        } else if (_view == mButtonPlay) {
            injectTouch(mClickList);
        } else if (_view == mButtonExit) {
            this.stopSelf();
        } else if (_view == mButtonScan) {
            takeScreenshot();
        }
    }

    private void injectTouch(final ArrayList<Point> points) {
        Thread task = new Thread(new Runnable() {
            private Handler handler;

            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (points != null) {
                            if (!points.isEmpty()) {
                                for (int i = 0; i < points.size(); i++) {
                                    Point p = points.get(i);
                                    doClick(p.x, p.y);
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }, 100);
                Looper.loop();
            }
        });
        task.start();
    }

    public void takeScreenshot() {
        BitmapUtil.takeScreenShot();
    }

    private void doClick(int _x, int _y) {
        Instrumentation m_Instrumentation = new Instrumentation();
        m_Instrumentation.setInTouchMode(false);
        m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, _x, _y, 0));
        m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, _x, _y, 0));
    }
}