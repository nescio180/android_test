package de.nescio.androidbot_test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.nescio.androidbottest.R;


public class MainActivity extends Activity implements View.OnClickListener {

    private Button mInjectButton;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInjectButton = (Button) findViewById(R.id.inject_btn);
        mInjectButton.setOnClickListener(this);

        mTextView = (TextView) findViewById(R.id.text_view);

    }

    public void startEventInject() {
        mTextView.requestFocus();
        final Thread t = new Thread() {
            public void run() {
            }
        };
        t.start();
    }

    @Override
    public void onClick(View view) {
        if (view == mInjectButton) {
            startEventInject();
        }
    }
}