package com.jasmeet.ttlsleep;

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var startTime: Time? = null
    private var endTime: Time? = null
    private lateinit var taskTextWatcher: TextWatcher
    private lateinit var db: AppDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // Remove App name on top left
        supportActionBar?.setDisplayShowTitleEnabled(false)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, ttl_widget_db_name
        ).build()

        setupDayStartEndTimeButtons()
        setupWidgetCreator()

        setupTasksTextBoxListener()
        val textBox = findViewById<TextInputEditText>(R.id.taskBoxTextInput)
        textBox.addTextChangedListener(taskTextWatcher)
    }

    private fun setupTasksTextBoxListener() {
        taskTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable?) {
                val linearLayout = findViewById<LinearLayout>(R.id.tasksContainer)
                if (text.isNullOrEmpty()) return

                val nextEditTextId = linearLayout.childCount
                if (nextEditTextId < 5) {
                    materializeNextTextBox(nextEditTextId)
                }
            }
        }
    }

    private fun materializeNextTextBox(nextEditTextId: Int) {
        // Make the next text box visible.
        val taskBox = findTaskBox(nextEditTextId)
        taskBox.visibility = View.VISIBLE
        val textInput = taskBox.findViewWithTag<TextInputEditText>("textInput")

        // Remove text input listener for previous input.
        val taskBoxTextInput = findTaskBox(nextEditTextId-1).findViewWithTag<TextInputEditText>("textInput")
        taskBoxTextInput.removeTextChangedListener(taskTextWatcher)

        if (nextEditTextId < 4) {
            textInput.addTextChangedListener(taskTextWatcher)
        }
    }

    private fun findTaskBox(id: Int): View {
        return findViewById<LinearLayout>(R.id.tasksContainer).getChildAt(id)
    }

    private fun setupDayStartEndTimeButtons() {
        val dayStartTimeButton = findViewById<Button>(R.id.day_start_time_button)
        val dayEndTimeButton = findViewById<Button>(R.id.day_end_time_button)

        dayStartTimeButton.setOnClickListener { openClockTicker(true, Time(9,0)) }
        dayEndTimeButton.setOnClickListener { openClockTicker(false, Time(23,30)) }
    }
     @RequiresApi(Build.VERSION_CODES.O)
     private fun setupWidgetCreator() {
         val addWidgetButton = findViewById<Button>(R.id.add_widget_button)
         addWidgetButton.setOnClickListener {
             lifecycleScope.launch {addWidget() }}
     }

    private fun openClockTicker(isStartTime: Boolean, initialTime: Time) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(initialTime.hour)
            .setMinute(initialTime.minute)
            .setTitleText("Select Time")
            .build()

        timePicker.show(supportFragmentManager, "timePickerTag")

        timePicker.addOnPositiveButtonClickListener {
            if (isStartTime) {
                startTime = Time(timePicker.hour, timePicker.minute)
            } else {
                endTime = Time(timePicker.hour, timePicker.minute)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun addWidget() {
        if (startTime == null || endTime == null) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Click them buttons")
                .setMessage("Please select day start/end time.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        if (!isStartTimeBeforeEndTime(startTime!!, endTime!!)) {
            MaterialAlertDialogBuilder(this)
                .setTitle("This app is for Mortals only")
                .setMessage("Day start time must be less than day end time.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val timeDao = db.timeDao()

        // Insert/Update entity
        val startTimeEntity = TimeDbEntity(day_start_time_db_key, startTime!!.toString())
        val endTimeEntity = TimeDbEntity(day_end_time_db_key, endTime!!.toString())
        timeDao.upsert(startTimeEntity)
        timeDao.upsert(endTimeEntity)

        // Open "Add Widget Dialog"
        val appWidgetManager = getSystemService(AppWidgetManager::class.java)
        val componentName = ComponentName(this, TTLWidgetProvider::class.java)
        appWidgetManager.requestPinAppWidget(componentName, null, null)
    }

    private fun isStartTimeBeforeEndTime(startTime: Time, endTime: Time): Boolean {
        return if (startTime.hour != endTime.hour) {
            startTime.hour < endTime.hour
        } else {
            startTime.minute < endTime.minute
        }
    }
}
