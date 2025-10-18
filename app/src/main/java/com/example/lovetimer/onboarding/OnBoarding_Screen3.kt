package com.example.lovetimer.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lovetimer.DateInputScreen
import com.example.lovetimer.MainActivity
import com.example.lovetimer.R
import com.example.lovetimer.databinding.ActivityOnBoardingScreen3Binding

class OnBoarding_Screen3 : AppCompatActivity() {
    private lateinit var binding: ActivityOnBoardingScreen3Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnBoardingScreen3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.button3.setOnClickListener {
            val intent = Intent(this, DateInputScreen::class.java)
            startActivity(intent)
            finish()
        }
    }
}