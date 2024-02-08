package com.jasmeet.ttlsleep.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.room.Room
import com.jasmeet.ttlsleep.MainActivity
import com.jasmeet.ttlsleep.R
import com.jasmeet.ttlsleep.cancelAlarm
import com.jasmeet.ttlsleep.database.AppDatabase
import com.jasmeet.ttlsleep.database.Time
import com.jasmeet.ttlsleep.getStartEndTime
import com.jasmeet.ttlsleep.setupAlarm
import com.jasmeet.ttlsleep.ttl_widget_db_name
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


data class StartEndTime(val startTime: Time, val endTime: Time)

class TTLWidgetProvider : AppWidgetProvider() {

    private suspend fun getStartEndTime(context: Context) : StartEndTime? {
        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, ttl_widget_db_name
        ).build()

        val startEndTime = getStartEndTime(db) ?: return null
        return StartEndTime(startEndTime.first, startEndTime.second)
    }

    private fun setTasksRemoteView(context: Context, rv: RemoteViews) {
        val intent = Intent(context, TaskListRemoteViewService::class.java)
        rv.setRemoteAdapter(R.id.widget_task_list, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val currentClass = javaClass

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val startEndTime = getStartEndTime(context) ?: return@launch
                withContext(Dispatchers.Main) {
                    val remoteViews = RemoteViews(context.packageName, R.layout.ttl_widget)
                    setTasksRemoteView(context, remoteViews)
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_task_list)

                    updateTimeView(startEndTime, remoteViews)

                    // Open main activity when widget is clicked
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    remoteViews.setOnClickPendingIntent(R.id.ttl_widget, pendingIntent)
                    remoteViews.setPendingIntentTemplate(R.id.widget_task_list, pendingIntent)

                    for (widgetId in appWidgetIds) {
                        appWidgetManager.updateAppWidget(widgetId, remoteViews)
                    }
                    setupAlarm(context, appWidgetIds, currentClass)
                }
            }
            catch (e: Exception) {
                Log.e("WidgetUpdate", "Error updating widget", e)
                // Handle exception
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        cancelAlarm(context, javaClass)
    }

    private fun updateTimeView(startEndTime: StartEndTime, remoteViews: RemoteViews) {
        val remainingTime = getRemainingTime(startEndTime.startTime, startEndTime.endTime)
        if (remainingTime.first != null) {
            remoteViews.setTextViewText(R.id.timeLeftLabel, remainingTime.first)
            remoteViews.setViewVisibility(R.id.timeLeftLabel, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.timeLeftIcon, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.timeLeftLabel, View.GONE)
            remoteViews.setViewVisibility(R.id.timeLeftIcon, View.VISIBLE)
        }
        // Progress bar is rendered in reverse (some rendering issue) so, just render 100 -x
        val progress = 100 - remainingTime.second
        remoteViews.setProgressBar(R.id.timeLeftBar, 100, progress, false)
    }

    private fun getRemainingTime(dayStartTime: Time, dayEndTime: Time): Pair<String?, Int> {
        val now = Calendar.getInstance()

        val startTimeCalendar = dayStartTime.toCalendar()
        val endTimeCalendar = dayEndTime.toCalendar()
        // Check if current time is after dayEndTime or before dayStartTime
        if (now.before(startTimeCalendar) && startTimeCalendar.before(endTimeCalendar)) {
            return Pair(null, 0)
        } else if (now.after(endTimeCalendar) && endTimeCalendar.after(startTimeCalendar)) {
            return Pair(null, 0)
        }

        // Adjust dayEndTime to the next day if it's already past
        if (now.after(endTimeCalendar)) {
            endTimeCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val diff = endTimeCalendar.timeInMillis - now.timeInMillis
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60

        val totalTimeDiff = endTimeCalendar.timeInMillis - startTimeCalendar.timeInMillis

        val percentageLeft = ((diff.toDouble() / totalTimeDiff) * 100).toInt()
        return Pair(String.format("%02dh:%02dm", hours, minutes), percentageLeft)
    }

}
