package com.example.ctrlaltelite;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;import android.os.Handler;
import android.os.Looper;


/**
 * The {@code SplashActivity} serves as the introductory screen displayed when the app launches.
 */

public class SplashActivity extends AppCompatActivity {

    LottieAnimationView lottie1;
    /**
     * Called when the activity is created.
     * Initializes the splash screen and starts a timer to transition to the main activity.
     *
     * @param savedInstanceState The saved instance state, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        lottie1 = findViewById(R.id.lottie1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lottie1.animate().translationX(2000).setDuration(2700).setStartDelay(2800);

        // Use a Handler to delay redirection instead of a separate Thread
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d("SplashActivity", "Redirecting to SignUp...");
            // Redirect to SignUp Activity after 5000ms
            Intent intent = new Intent(SplashActivity.this, SignUp.class);
            startActivity(intent);
            finish();  // Close SplashActivity so the user cannot go back to it
        }, 5000);
    }
}