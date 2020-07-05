package com.example.miniproject.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.example.miniproject.NativeClasses.Model;
import com.example.miniproject.R;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SECONDS_DELAY = 5;
    static {
        System.loadLibrary("native-lib");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //startActivity(new Intent(SplashActivity.this, MainActivity.class));
        Model model = new Model(this);
        model.start();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Log.d(TAG,"Splash Activity " + SECONDS_DELAY + " sec");
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, SECONDS_DELAY * 1000);

    }
}