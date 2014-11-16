package de.nescio.androidbot_test.service;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Environment;
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
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import de.nescio.androidbot_test.utils.BitmapUtil;
import de.nescio.androidbot_test.utils.Point;
import de.nescio.androidbottest.R;

/**
 * Created by Wohnzimmer on 15.11.2014.
 */
public class OverlayShowingService extends Service implements View.OnTouchListener, OnClickListener {
    private WindowManager mWindowManager;
    private Button mButtonRec, mButtonPlay, mButtonExit;
    private View topLeftView;
    private View mMainView;
    private FrameLayout mTouchableView;
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;
    private boolean mRecordTouch;
    private volatile ArrayList<Point> mClickList = new ArrayList<Point>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mMainView = inflater.inflate(R.layout.overlay_view, null);
        mMainView.setOnTouchListener(this);

        mButtonPlay = (Button) mMainView.findViewById(R.id.overlay_button_play);
        mButtonExit = (Button) mMainView.findViewById(R.id.overlay_button_exit);
        mButtonRec = (Button) mMainView.findViewById(R.id.overlay_button_rec);

        mButtonPlay.setOnClickListener(this);
        mButtonExit.setOnClickListener(this);
        mButtonRec.setOnClickListener(this);

        mTouchableView = new FrameLayout(this);
        mTouchableView.setVisibility(View.INVISIBLE);
        mTouchableView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    int touchX = (int) motionEvent.getRawX();
                    int touchY = (int) motionEvent.getRawY();
                    if (mRecordTouch) {
                        mClickList.add(new Point(touchX, touchY));
                    }
                }
                return false;
            }
        });

        WindowManager.LayoutParams touchableParams = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL| LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        touchableParams.gravity = Gravity.LEFT | Gravity.TOP;
        touchableParams.x = 0;
        touchableParams.y = 0;
        mWindowManager.addView(mTouchableView, touchableParams);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL| LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        mWindowManager.addView(mMainView, params);

        topLeftView = new View(this);
        WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL| LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
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

    private void toggleRecording(boolean _enabled) {
        if (_enabled) {
            mRecordTouch = false;
            mTouchableView.setVisibility(View.INVISIBLE);
            injectTouch(mClickList);
        } else {
            mRecordTouch = true;
            mTouchableView.setVisibility(View.VISIBLE);
        }
    }

    private void toggleRecording() {
        toggleRecording(mRecordTouch);
    }


    @Override
    public void onClick(View _view) {
        if (_view == mButtonRec) {
            toggleRecording();
        } else if (_view == mButtonPlay) {
            scanScreen();
        } else if (_view == mButtonExit) {
            this.stopSelf();
        }
    }

    private void saveBitmapToSD(Bitmap b) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + "test.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
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
                                        Thread.sleep(50);
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

    public void scanScreen() {
        ArrayList<Point> points = new ArrayList<Point>();
        Bitmap screen = BitmapUtil.getScreenBitmap();
        int pixel[] = BitmapUtil.getBitmapPixel(screen);
        for (int i = 0; i < pixel.length; i++) {
            int currentPixel = pixel[i];
            int redValue = Color.red(currentPixel);
            int blueValue = Color.blue(currentPixel);
            int greenValue = Color.green(currentPixel);
            if (redValue >= 230 && redValue <= 250 &&
                    greenValue >= 220 && greenValue <= 240 &&
                    blueValue >= 70 && blueValue <= 90) {
                points.add(pixelToPoint(currentPixel, screen, pixel.length, i));
            }
        }

        mTouchableView.removeAllViews();

        for (Point p : points) {
            ImageView v = new ImageView(this);
            v.setLayoutParams(new FrameLayout.LayoutParams(10, 10));
            v.setBackgroundColor(Color.CYAN);
            v.setX(p.x);
            v.setY(p.y);
            mTouchableView.addView(v);
        }

        mClickList = refinedPoints(points);
    }

    private ArrayList<Point> refinedPoints(ArrayList<Point> points) {
        ArrayList<Point> newPoints = new ArrayList<Point>();
        

        return null;
    }

    private Point pixelToPoint(int pixel, Bitmap b, int pixelCount, int position) {
        int width = b.getWidth();
        int height = b.getHeight();

        // TODO
        int x = (height - position / width);
        int y = (position % width);

        Point p = new Point(x, y);
        return p;
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