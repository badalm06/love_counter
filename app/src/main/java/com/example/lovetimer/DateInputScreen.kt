package com.example.lovetimer

import WheelAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import java.util.Calendar

class DateInputScreen : AppCompatActivity() {

    private lateinit var rvMonth: RecyclerView
    private lateinit var rvDay: RecyclerView
    private lateinit var rvYear: RecyclerView

    private lateinit var rvHour: RecyclerView
    private lateinit var rvMinute: RecyclerView
    private lateinit var rvAmPm: RecyclerView

    private lateinit var monthAdapter: WheelAdapter
    private lateinit var dayAdapter: WheelAdapter
    private lateinit var yearAdapter: WheelAdapter

    private lateinit var hourAdapter: WheelAdapter
    private lateinit var minuteAdapter: WheelAdapter
    private lateinit var ampmAdapter: WheelAdapter

    private val months = listOf(
        "","January","February","March","April","May","June",
        "July","August","September","October","November","December",""
    )
    private var days = listOf("") + (1..31).map { it.toString() } + listOf("")
    private var years = listOf("") + (1900..2100).map { it.toString() } + listOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_input_screen)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("Date"))
        tabLayout.addTab(tabLayout.newTab().setText("Time"))

        // find views
        rvMonth = findViewById(R.id.rv_month)
        rvDay = findViewById(R.id.rv_day)
        rvYear = findViewById(R.id.rv_year)

        rvHour = findViewById(R.id.rv_hour)
        rvMinute = findViewById(R.id.rv_minute)
        rvAmPm = findViewById(R.id.rv_ampm)

        setupDateWheels()
        setupTimeWheels()

        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                val index = tab.position
                findViewById<View>(R.id.dateWheelsLayout).visibility = if (index==0) View.VISIBLE else View.GONE
                findViewById<View>(R.id.timeWheelsLayout).visibility = if (index==1) View.VISIBLE else View.GONE
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        findViewById<Button>(R.id.btn_get_started).setOnClickListener {
            val selectedMonth = getCenteredItem(rvMonth)
            val selectedDay = getCenteredItem(rvDay)
            val selectedYear = getCenteredItem(rvYear)

            val selectedHour = getCenteredItem(rvHour)
            val selectedMin = getCenteredItem(rvMinute)
            val selectedAmPm = getCenteredItem(rvAmPm)

            if (selectedMonth.isNotEmpty() && selectedDay.isNotEmpty() && selectedYear.isNotEmpty()) {
                // Save date & time locally
                val prefs = getSharedPreferences("LoveTimerPrefs", MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putString("month", selectedMonth)
                editor.putString("day", selectedDay)
                editor.putString("year", selectedYear)
                editor.putString("hour", selectedHour)
                editor.putString("minute", selectedMin)
                editor.putString("ampm", selectedAmPm)
                editor.apply()

                android.widget.Toast.makeText(this, "Date Saved Successfully 💖", android.widget.Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

                // You can navigate to your main screen here
                // startActivity(Intent(this, MainScreen::class.java))
            } else {
                android.widget.Toast.makeText(this, "Please select full date & time!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDateWheels() {
        // Adapters
        monthAdapter = WheelAdapter(months)
        dayAdapter = WheelAdapter(days)
        yearAdapter = WheelAdapter(years)

        // LayoutManagers
        rvMonth.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvDay.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvYear.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        rvMonth.adapter = monthAdapter
        rvDay.adapter = dayAdapter
        rvYear.adapter = yearAdapter

        // SnapHelper to center items
        val snapMonth = LinearSnapHelper()
        val snapDay = LinearSnapHelper()
        val snapYear = LinearSnapHelper()
        snapMonth.attachToRecyclerView(rvMonth)
        snapDay.attachToRecyclerView(rvDay)
        snapYear.attachToRecyclerView(rvYear)

        // scroll listeners to update day count when month/year change
        addOnSnapScrollListener(rvMonth) { pos ->
            // when month changed
            updateDaysForMonth(pos, getCenteredPosition(rvYear))
        }
        addOnSnapScrollListener(rvYear) { pos ->
            // when year changed
            updateDaysForMonth(getCenteredPosition(rvMonth), pos)
        }

        // pre-scroll to today's date (optional)
        val cal = Calendar.getInstance()
        rvMonth.scrollToPosition(cal.get(Calendar.MONTH))
        rvDay.scrollToPosition(cal.get(Calendar.DAY_OF_MONTH)-1)
        rvYear.scrollToPosition(years.indexOf(cal.get(Calendar.YEAR).toString()))
    }

    private fun setupTimeWheels() {
        val hours = listOf("") + (1..12).map { it.toString().padStart(2, '0') } + listOf("")
        val mins = listOf("") + (0..59).map { it.toString().padStart(2, '0') } + listOf("")
        val ampm = listOf("", "AM", "PM", "")


        hourAdapter = WheelAdapter(hours)
        minuteAdapter = WheelAdapter(mins)
        ampmAdapter = WheelAdapter(ampm)

        rvHour.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvMinute.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvAmPm.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        rvHour.adapter = hourAdapter
        rvMinute.adapter = minuteAdapter
        rvAmPm.adapter = ampmAdapter

        LinearSnapHelper().attachToRecyclerView(rvHour)
        LinearSnapHelper().attachToRecyclerView(rvMinute)
        LinearSnapHelper().attachToRecyclerView(rvAmPm)

        // default to current time
        val cal = Calendar.getInstance()
        var hour = cal.get(Calendar.HOUR)
        if (hour == 0) hour = 12
        val minute = cal.get(Calendar.MINUTE)
        val isAm = cal.get(Calendar.AM_PM) == Calendar.AM

        rvHour.scrollToPosition(hours.indexOf(hour.toString().padStart(2,'0')))
        rvMinute.scrollToPosition(minute)
        rvAmPm.scrollToPosition(if (isAm) 0 else 1)
    }

    private fun updateDaysForMonth(monthPosition: Int, yearPosition: Int) {
        if (monthPosition < 0 || yearPosition < 0) return
        val month = monthPosition // 0-based
        val year = years[yearPosition].toInt()
        val maxDays = when (month) {
            0,2,4,6,7,9,11 -> 31
            3,5,8,10 -> 30
            1 -> if (isLeapYear(year)) 29 else 28
            else -> 31
        }
        val newDays = listOf("") + (1..maxDays).map { it.toString() } + listOf("")
        dayAdapter.update(newDays)
        // if current centered day is greater than newDays -> snap to last
        val centered = getCenteredPosition(rvDay)
        if (centered >= newDays.size) {
            rvDay.scrollToPosition(newDays.size - 1)
        }
    }

    private fun isLeapYear(y: Int): Boolean {
        return (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
    }

    /** helper to get visible centered item text */
    private fun getCenteredItem(rv: RecyclerView): String {
        val snap = LinearSnapHelper()
        // find view snapped by using existing SnapHelper attached (we used same instance variable earlier)
        val layoutManager = rv.layoutManager as LinearLayoutManager
        val mid = rv.height / 2
        val centerView = rv.findChildViewUnder((rv.width/2).toFloat(), (mid).toFloat())
        // fallback: use first visible
        val pos = when {
            centerView != null -> rv.getChildAdapterPosition(centerView)
            else -> layoutManager.findFirstVisibleItemPosition()
        }
        val count = rv.adapter?.itemCount ?: 0
        return if (pos in 1 until (count - 1)) { // skip blanks
            when (rv.id) {
                R.id.rv_month -> months[pos]
                R.id.rv_day -> days[pos]
                R.id.rv_year -> years[pos]
                R.id.rv_hour -> (listOf("") + (1..12).map { it.toString().padStart(2,'0') } + listOf(""))[pos]
                R.id.rv_minute -> (listOf("") + (0..59).map { it.toString().padStart(2,'0') } + listOf(""))[pos]
                R.id.rv_ampm -> listOf("", "AM", "PM", "")[pos]
                else -> ""
            }
        }
        else ""
    }

    // get centered index using snap helper logic
    private fun getCenteredPosition(rv: RecyclerView): Int {
        val lm = rv.layoutManager as LinearLayoutManager
        val first = lm.findFirstVisibleItemPosition()
        val last = lm.findLastVisibleItemPosition()
        if (first == RecyclerView.NO_POSITION) return -1
        val centerY = rv.height / 2
        var bestPos = -1
        var minDistance = Int.MAX_VALUE
        for (i in first..last) {
            val child = rv.findViewHolderForAdapterPosition(i)?.itemView ?: continue
            val childCenter = (child.top + child.bottom) / 2
            val d = Math.abs(childCenter - centerY)
            if (d < minDistance) {
                minDistance = d
                bestPos = i
            }
        }
        return bestPos
    }

    private fun addOnSnapScrollListener(rv: RecyclerView, onCenterChanged: (Int) -> Unit) {
        var lastCenter = -1
        rv.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val pos = getCenteredPosition(rv)
                    if (pos != lastCenter) {
                        lastCenter = pos
                        onCenterChanged(pos)
                    }
                }
            }
        })
    }
}