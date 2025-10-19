package com.example.lovetimer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lovetimer.onboarding.OnBoarding_Screen1

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("LoveTimerPrefs", MODE_PRIVATE)
            val month = prefs.getString("month", null)
            val day = prefs.getString("day", null)
            val year = prefs.getString("year", null)

            if (month != null && day != null && year != null) {
                // User already saved a date → Go to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // First time user → Go to Onboarding
                startActivity(Intent(this, OnBoarding_Screen1::class.java))
            }
            finish()
        }, 2000)
    }
}
