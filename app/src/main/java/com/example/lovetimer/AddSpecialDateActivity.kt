package com.example.lovetimer

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AddSpecialDateActivity : AppCompatActivity() {

    private lateinit var btnPickDate: Button
    private lateinit var tvSelectedDate: TextView
    private lateinit var etNote: EditText
    private lateinit var btnSave: Button
    private lateinit var rvSpecialDates: RecyclerView

    private var selectedDate: Calendar? = null
    private val savedDates = mutableListOf<SpecialDateEntry>()
    private lateinit var adapter: SpecialDatesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_special_date)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnPickDate = findViewById(R.id.btn_pick_date)
        tvSelectedDate = findViewById(R.id.tv_selected_date)
        etNote = findViewById(R.id.et_note)
        btnSave = findViewById(R.id.btn_save)
        rvSpecialDates = findViewById(R.id.rv_special_dates)

        // Initialize adapter BEFORE loading saved dates
        adapter = SpecialDatesAdapter(savedDates) { position ->
            val removed = savedDates.removeAt(position)
            adapter.notifyItemRemoved(position)
            saveToPrefs()
            Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show()
        }
        rvSpecialDates.layoutManager = LinearLayoutManager(this)
        rvSpecialDates.adapter = adapter

        loadSavedDates()  // Now safe, adapter is ready

        btnPickDate.setOnClickListener { showDatePicker() }
        btnSave.setOnClickListener { saveDateNote() }

        findViewById<ImageView>(R.id.back_button1).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    private fun showDatePicker() {
        val cal = selectedDate ?: Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val calNew = Calendar.getInstance()
            calNew.set(year, month, dayOfMonth)
            // After date picked, show time picker
            showTimePicker(calNew)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(calendar: Calendar) {
        val cal = calendar
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        android.app.TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            cal.set(Calendar.HOUR_OF_DAY, selectedHour)
            cal.set(Calendar.MINUTE, selectedMinute)
            selectedDate = cal
            // Format and display combined date and time
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            tvSelectedDate.text = sdf.format(cal.time)
        }, hour, minute, false).show()  // false for 12-hour format
    }

    private fun saveDateNote() {
        val cal = selectedDate
        if (cal == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }
        val sdfStore = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateStr = sdfStore.format(cal.time)
        val noteText = etNote.text.toString()

        val newEntry = SpecialDateEntry(dateStr, noteText)
        if (savedDates.none { it.dateTimeKey == dateStr }) {
            savedDates.add(0, newEntry)  // adds new entry at index 0 (top)
            adapter.notifyItemInserted(0)
            rvSpecialDates.scrollToPosition(0)  // scroll to show the newly added item at top
            saveToPrefs()
            Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show()
            etNote.setText("")
            tvSelectedDate.text = "No date selected"
            selectedDate = null
        } else {
            Toast.makeText(this, "This date already exists", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToPrefs() {
        val prefs = getSharedPreferences("LoveTimerPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        val allEntries = savedDates.joinToString("|") {
            "${it.dateTimeKey}~${it.note}"
        }
        editor.putString("special_dates", allEntries)
        editor.apply()
    }

    private fun loadSavedDates() {
        val prefs = getSharedPreferences("LoveTimerPrefs", MODE_PRIVATE)
        val data = prefs.getString("special_dates", null)
        if (!data.isNullOrEmpty()) {
            val entries = data.split("|")
            for (entry in entries) {
                val parts = entry.split("~")
                if (parts.size == 2) {
                    savedDates.add(SpecialDateEntry(parts[0], parts[1]))
                }
            }
        }
        adapter.notifyDataSetChanged()
    }
}

// Data class for each special date note
data class SpecialDateEntry(val dateTimeKey: String, val note: String)

// Adapter to show special dates
class SpecialDatesAdapter(
    private val items: MutableList<SpecialDateEntry>,
    private val onDeleteClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<SpecialDatesViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): SpecialDatesViewHolder {
        val view = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_special_date, parent, false)
        return SpecialDatesViewHolder(view, onDeleteClicked)
    }

    override fun onBindViewHolder(holder: SpecialDatesViewHolder, position: Int) {
        val item = items[position]
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val displaySdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        val date = try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.dateTimeKey)
        } catch (e: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(item.dateTimeKey)
            } catch (ex: Exception) {
                null
            }
        }
        val dateStr = date?.let { displaySdf.format(it) } ?: item.dateTimeKey

        holder.tvDate.text = dateStr
        holder.tvNote.text = item.note.ifEmpty { "No note" }
    }

    override fun getItemCount(): Int = items.size
}

class SpecialDatesViewHolder(itemView: android.view.View, onDeleteClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
    val tvDate: TextView = itemView.findViewById(R.id.tv_date)
    val tvNote: TextView = itemView.findViewById(R.id.tv_note)
    private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)

    init {
        btnDelete.setOnClickListener {
            onDeleteClicked(adapterPosition)
        }
    }
}

