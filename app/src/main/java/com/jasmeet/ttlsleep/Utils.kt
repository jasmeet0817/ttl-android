package com.jasmeet.ttlsleep

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jasmeet.ttlsleep.database.AppDatabase
import com.jasmeet.ttlsleep.database.Time
import com.jasmeet.ttlsleep.widget.TTLWidgetProvider

const val ttl_widget_db_name = "TTLWidget"
const val day_start_time_db_key = "day_start_time"
const val day_end_time_db_key = "day_end_time"
const val tasks_db_key = "tasks"

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Perform the actual migration
        // database.execSQL("ALTER TABLE table_name ADD COLUMN new_column_name COLUMN_TYPE")
    }
}

// Alarms are needed to update the widget on a frequent interval.
fun setupAlarm(context: Context, appWidgetIds: IntArray, javaClass: Class<TTLWidgetProvider>) {
    // Set up the intent that starts the AlarmManager service
    val intent = Intent(context, javaClass)
    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Get the AlarmManager service
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Set the alarm to trigger approximately every minute (60000 milliseconds)
    val interval: Long = 120000
    alarmManager.setRepeating(
        AlarmManager.RTC,
        System.currentTimeMillis(),
        interval,
        pendingIntent
    )
}

fun cancelAlarm(context: Context, javaClass: Class<TTLWidgetProvider>) {
    // Cancel the alarm
    val intent = Intent(context, javaClass)
    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
}

suspend fun getStartEndTime(db: AppDatabase): Pair<Time, Time>? {
    val timeDao = db.timeDao()
    val startTime = timeDao.getTime(day_start_time_db_key) ?: return null
    val endTime =  timeDao.getTime(day_end_time_db_key) ?: return null
    return Pair(Time.fromString(startTime.timeString), Time.fromString(endTime.timeString))
}