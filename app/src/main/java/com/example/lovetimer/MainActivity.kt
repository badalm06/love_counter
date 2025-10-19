package com.example.lovetimer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

class MainActivity : AppCompatActivity() {

    private lateinit var tvYears: TextView
    private lateinit var tvMonths: TextView
    private lateinit var tvDays: TextView
    private lateinit var tvHours: TextView
    private lateinit var tvMinutes: TextView
    private lateinit var tvSeconds: TextView

    private lateinit var quoteText: TextView // <-- Add this reference

    private val handler = Handler(Looper.getMainLooper())
    private var startDate: Date? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvYears = findViewById(R.id.tv_years)
        tvMonths = findViewById(R.id.tv_months)
        tvDays = findViewById(R.id.tv_days)
        tvHours = findViewById(R.id.tv_hours)
        tvMinutes = findViewById(R.id.tv_minutes)
        tvSeconds = findViewById(R.id.tv_seconds)

        quoteText = findViewById(R.id.quoteText)

        // Show daily quote (rotates automatically)
        val daysSinceEpoch = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt()
        val dailyQuote = QuotesProvider.quotes[daysSinceEpoch % QuotesProvider.quotes.size]
        quoteText.text = dailyQuote


        // Retrieve stored values
        val prefs = getSharedPreferences("LoveTimerPrefs", MODE_PRIVATE)
        val month = prefs.getString("month", null)
        val day = prefs.getString("day", null)
        val year = prefs.getString("year", null)
        val hour = prefs.getString("hour", "12")
        val minute = prefs.getString("minute", "00")
        val ampm = prefs.getString("ampm", "AM")

        if (month != null && day != null && year != null) {
            val dateString = "$month $day, $year $hour:$minute $ampm"
            val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            startDate = sdf.parse(dateString)
        }

        startDate?.let {
            startUpdatingTimer()
        }
    }

    private fun startUpdatingTimer() {
        handler.post(object : Runnable {
            override fun run() {
                startDate?.let { updateLoveDuration(it) }
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun updateLoveDuration(startDate: Date) {
        val now = Date()
        val diffInMillis = now.time - startDate.time

        // Convert milliseconds to total seconds
        val totalSeconds = diffInMillis / 1000
        val totalMinutes = floor(totalSeconds / 60.0).toLong()
        val totalHours = floor(totalMinutes / 60.0).toLong()
        val totalDays = floor(totalHours / 24.0).toLong()

        // Calculate breakdown
        val years = (totalDays / 365)
        val remainingDaysAfterYears = (totalDays % 365)
        val months = (remainingDaysAfterYears / 30)
        val remainingDays = (remainingDaysAfterYears % 30)

        val remainingHours = (totalHours % 24)
        val remainingMinutes = (totalMinutes % 60)
        val remainingSeconds = (totalSeconds % 60)

        // Display results line by line
        tvYears.text = "$years years"
        tvMonths.text = "$months months"
        tvDays.text = "$remainingDays days"
        tvHours.text = "$remainingHours hours"
        tvMinutes.text = "$remainingMinutes minutes"
        tvSeconds.text = "$remainingSeconds seconds"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
