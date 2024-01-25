package com.jasmeet.ttlsleep

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent

const val ttl_widget_db_name = "TTLWidget"
const val day_start_time_db_key = "day_start_time"
const val day_end_time_db_key = "day_end_time"

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