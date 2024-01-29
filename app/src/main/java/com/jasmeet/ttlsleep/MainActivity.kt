package com.jasmeet.ttlsleep;

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.jasmeet.ttlsleep.database.AppDatabase
import com.jasmeet.ttlsleep.database.DatabaseModule
import com.jasmeet.ttlsleep.database.DatabaseSingleton
import com.jasmeet.ttlsleep.database.Time
import com.jasmeet.ttlsleep.database.TimeDbEntity
import com.jasmeet.ttlsleep.tasks.TasksManager
import com.jasmeet.ttlsleep.widget.TTLWidgetProvider
import com.jasmeet.ttlsleep.widget.UpdateWidgetCallback
import com.jasmeet.ttlsleep.widget.WidgetManager
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : AppCompatActivity(), UpdateWidgetCallback {
    @Inject
    lateinit var databaseSingleton: DatabaseSingleton

    private lateinit var db: AppDatabase
    private lateinit var tasksManager: TasksManager

    private var startTime: Time? = null
    private var endTime: Time? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DaggerAppComponent.builder()
            .databaseModule(DatabaseModule(applicationContext))
            .build()
            .inject(this)

        db = databaseSingleton.getAppDatabase()
        tasksManager = TasksManager(db,this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // Remove App name on top left
        supportActionBar?.setDisplayShowTitleEnabled(false)

        lifecycleScope.launch {
            val startEndTime = getStartEndTime(db)
            startTime = startEndTime?.first
            endTime = startEndTime?.second
        }
        setupDayStartEndTimeButtons()

        setupWidgetCreator()

        tasksManager.setupTasks()
    }

    private fun setupDayStartEndTimeButtons() {
        val dayStartTimeButton = findViewById<Button>(R.id.day_start_time_button)
        val dayEndTimeButton = findViewById<Button>(R.id.day_end_time_button)

        dayStartTimeButton.setOnClickListener { openClockTicker(true, Time(9, 0)) }
        dayEndTimeButton.setOnClickListener { openClockTicker(false, Time(23, 30)) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupWidgetCreator() {
        val addWidgetButton = findViewById<Button>(R.id.add_widget_button)
        addWidgetButton.setOnClickListener {
            lifecycleScope.launch { addWidget() }
        }
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
        onUpdateWidgetMessage()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun addWidget() {
        if (startTime == null || endTime == null) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Click them time buttons")
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

    override fun onUpdateWidgetMessage() {
        WidgetManager.updateWidget(applicationContext)
    }
}
