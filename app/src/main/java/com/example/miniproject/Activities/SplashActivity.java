package com.example.miniproject.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.miniproject.R;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SECONDS_DELAY = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        startActivity(new Intent(SplashActivity.this, MainActivity.class));

/*        new Handler().postDelayed(new Runnable() {
            public void run() {
                Log.d(TAG,"Splash Activity " + SECONDS_DELAY + " sec");
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, SECONDS_DELAY * 1000);*/

    }
}