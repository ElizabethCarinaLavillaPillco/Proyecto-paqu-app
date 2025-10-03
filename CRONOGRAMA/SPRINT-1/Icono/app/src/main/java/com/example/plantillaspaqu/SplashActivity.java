package com.example.plantillaspaqu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 segundos
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable goMain = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView splash = findViewById(R.id.splashAnimation);
        splash.playAnimation();

        handler.postDelayed(goMain, SPLASH_DELAY);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(goMain);
        super.onDestroy();
    }
}
