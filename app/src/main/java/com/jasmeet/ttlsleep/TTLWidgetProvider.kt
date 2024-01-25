package com.jasmeet.ttlsleep

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import androidx.room.Room
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar

class TTLWidgetProvider : AppWidgetProvider() {

    private suspend fun getStartEndTime(context: Context) : Pair<Time, Time>? {
        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, ttl_widget_db_name
        ).build()
        val timeDao = db.timeDao()
        val startTime = timeDao.getTime(day_start_time_db_key)
        val endTime =  timeDao.getTime(day_end_time_db_key)
        if (startTime == null || endTime == null) {
            return null
        }
        return Pair(Time.fromString(startTime.timeString), Time.fromString(endTime.timeString))

    }
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d("TTLWidget", "here 1")

        val currentClass = javaClass

        GlobalScope.launch {
            val startEndTime = getStartEndTime(context) ?: return@launch
            Log.d("TTLWidget", "here 2")
            for (widgetId in appWidgetIds) {
                val remoteViews = RemoteViews(context.packageName, R.layout.ttl_widget)
                remoteViews.setTextViewText(R.id.timeLeft, getRemainingTime(startEndTime.first, startEndTime.second))

                appWidgetManager.updateAppWidget(widgetId, remoteViews)
            }
            setupAlarm(context, appWidgetIds, currentClass)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        cancelAlarm(context, javaClass)
    }

    private fun getRemainingTime(dayStartTime: Time, dayEndTime: Time): String {
        val now = Calendar.getInstance()

        val startTimeCalendar = dayStartTime.toCalendar()
        val endTimeCalendar = dayEndTime.toCalendar()
        // Check if current time is after dayEndTime or before dayStartTime
        if (now.before(startTimeCalendar) && now.after(endTimeCalendar)) {
            return "Day is already over"
        }

        // Adjust dayEndTime to the next day if it's already past
        if (now.after(endTimeCalendar)) {
            endTimeCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val diff = endTimeCalendar.timeInMillis - now.timeInMillis
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60

        return String.format("%02d hours and %02d minutes left", hours, minutes)
    }

}
