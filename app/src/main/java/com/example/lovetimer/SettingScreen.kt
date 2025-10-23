package com.example.lovetimer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*

class SettingScreen : AppCompatActivity() {

    private lateinit var metOnDateTime: TextView
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        metOnDateTime = findViewById(R.id.metOnDateTime)
        prefs = getSharedPreferences("LoveTimerPrefs", MODE_PRIVATE)

        // Load and display stored date/time
        displaySavedDate()

        // Back navigation
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Edit date
        findViewById<Button>(R.id.editDateButton).setOnClickListener {
            startActivity(Intent(this, DateInputScreen::class.java))
        }

    }

    private fun displaySavedDate() {
        val month = prefs.getString("month", "August")
        val day = prefs.getString("day", "13")
        val year = prefs.getString("year", "2025")
        val hour = prefs.getString("hour", "09")
        val minute = prefs.getString("minute", "21")
        val ampm = prefs.getString("ampm", "AM")
        val formattedDate = "The $day${getDaySuffix(day)} of $month, $year at $hour:$minute $ampm"
        metOnDateTime.text = formattedDate
    }
    // Utility to get day suffix ("st", "nd", "rd", "th")
    private fun getDaySuffix(dayStr: String?): String {
        val day = dayStr?.toIntOrNull() ?: return ""
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }

}
