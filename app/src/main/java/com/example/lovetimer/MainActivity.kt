package com.example.lovetimer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        findViewById<ImageView>(R.id.settings_button).setOnClickListener {
            startActivity(Intent(this, SettingScreen::class.java))
        }

        findViewById<Button>(R.id.btn_add_special_date).setOnClickListener {
            startActivity(Intent(this, AddSpecialDateActivity::class.java))
        }

        // Retrieve stored values
        val prefs = getSharedPreferences("LoveTimerPrefs", MODE_PRIVATE)

        val month = prefs.getString("month", null)
        val day = prefs.getString("day", null)
        val year = prefs.getString("year", null)

        // Get stored time parts, allow empty or null
        var hour = prefs.getString("hour", null)
        var minute = prefs.getString("minute", null)
        var ampm = prefs.getString("ampm", null)

        // Get current local time parts
        val cal = Calendar.getInstance()
        val currentHour = if (cal.get(Calendar.HOUR) == 0) 12 else cal.get(Calendar.HOUR)
        val currentMinute = cal.get(Calendar.MINUTE)
        val currentAmPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"

        // Replace empty or null time parts with current time counterparts
        if (hour.isNullOrEmpty() || hour == "") hour = currentHour.toString().padStart(2, '0')
        if (minute.isNullOrEmpty() || minute == "") minute = currentMinute.toString().padStart(2, '0')
        if (ampm.isNullOrEmpty() || ampm == "") ampm = currentAmPm

        if (month != null && day != null && year != null) {
            val dateString = "$month $day, $year $hour:$minute $ampm"
            val sdf = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault())
            try {
                startDate = sdf.parse(dateString)
                startUpdatingTimer()
            } catch (e: Exception) {
                e.printStackTrace()
                startDate = null
            }
        } else {
            startDate = null
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