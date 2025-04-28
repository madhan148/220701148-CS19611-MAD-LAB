package com.example.notable

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: NotableDbHelper
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = NotableDbHelper(this)

        val occasionEditText = findViewById<EditText>(R.id.occasionEditText)
        val dateEditText = findViewById<EditText>(R.id.dateEditText)
        val saveButton = findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            val name = occasionEditText.text.toString().trim()
            val dateInput = dateEditText.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Occasion name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dateInput.isEmpty()) {
                Toast.makeText(this, "Date cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val date = LocalDate.parse(dateInput, dateFormatter)
                dbHelper.addEvent(name, date)
                occasionEditText.text.clear()
                dateEditText.text.clear()
                updateDatesView()
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            }
        }

        updateDatesView()
    }

    private fun updateDatesView() {
        val container = findViewById<LinearLayout>(R.id.eventsContainer)
        container.removeAllViews()

        val events = dbHelper.getAllEvents()
        if (events.isEmpty()) {
            val textView = TextView(this).apply {
                text = "No dates stored yet."
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
            }
            container.addView(textView)
            return
        }

        events.forEach { (name, date) ->
            val entryLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16.dpToPx())
                }
            }

            val eventText = TextView(this).apply {
                text = "$name - ${date.format(dateFormatter)}"
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
            }

            val deleteButton = Button(this).apply {
                text = "Delete"
                setOnClickListener {
                    dbHelper.deleteEvent(name, date)
                    updateDatesView()
                }
            }

            entryLayout.addView(eventText)
            entryLayout.addView(deleteButton)
            container.addView(entryLayout)
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}