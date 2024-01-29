package com.jasmeet.ttlsleep.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.RemoteViews
import androidx.room.Room
import com.jasmeet.ttlsleep.R
import com.jasmeet.ttlsleep.cancelAlarm
import com.jasmeet.ttlsleep.database.AppDatabase
import com.jasmeet.ttlsleep.database.Tasks
import com.jasmeet.ttlsleep.database.Time
import com.jasmeet.ttlsleep.getStartEndTime
import com.jasmeet.ttlsleep.setupAlarm
import com.jasmeet.ttlsleep.tasks_db_key
import com.jasmeet.ttlsleep.ttl_widget_db_name
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


data class DatabaseObjects(val startTime: Time, val endTime: Time, val tasks: Tasks)

class TTLWidgetProvider : AppWidgetProvider() {

    private val maxTasksInWidget = 3

    private suspend fun getDbObjects(context: Context) : DatabaseObjects? {
        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, ttl_widget_db_name
        ).build()

        val startEndTime = getStartEndTime(db) ?: return null
        val tasks = getTasks(db) ?: return null
        return DatabaseObjects(startEndTime.first, startEndTime.second, tasks)
    }

    private suspend fun getTasks(db: AppDatabase): Tasks? {
        val tasksDao = db.tasksDao()
        val tasks = tasksDao.getTasks(tasks_db_key) ?: return null
        return Tasks.fromString(tasks.serializedTasks)
    }

    private fun setTasks(context: Context, remoteViews : RemoteViews, dbObjects : DatabaseObjects) {
        remoteViews.setRemoteAdapter(R.id.taskList, )

        // Template to handle the click listener for each item
        val clickIntentTemplate = Intent(context, MyWidgetProvider::class.java)
        val clickPendingIntentTemplate = PendingIntent.getBroadcast(context, 0, clickIntentTemplate, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setPendingIntentTemplate(R.id.taskList, clickPendingIntentTemplate)

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)



        val tasks = dbObjects.tasks.getTasks(maxTasksInWidget)
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, tasks)
        val listView: ListView = findViewById(R.id.taskList)
        listView.adapter = adapter
//        val taskIds = arrayOf(R.id.task1, R.id.task2, R.id.task3)
//        for ((i, task) in dbObjects.tasks.getTasks(maxTasksInWidget).withIndex()) {
//            remoteViews.setTextViewText(
//                taskIds[i],
//                task
//            )
//            (taskIds[i] as TextView).visibility = View.VISIBLE
//        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val currentClass = javaClass

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbObjects = getDbObjects(context) ?: return@launch
                withContext(Dispatchers.Main) {
            for (widgetId in appWidgetIds) {
                val remoteViews = RemoteViews(context.packageName, R.layout.ttl_widget)

                setTasks(remoteViews, dbObjects)
                val remainingTime = getRemainingTime(dbObjects.startTime, dbObjects.endTime)
                if (remainingTime.first != null) {
                    remoteViews.setTextViewText(R.id.timeLeftLabel, remainingTime.first)
                    remoteViews.setViewVisibility(R.id.timeLeftLabel, View.VISIBLE)
                    remoteViews.setViewVisibility(R.id.timeLeftIcon, View.GONE)
                } else {
                    remoteViews.setViewVisibility(R.id.timeLeftLabel, View.GONE)
                    remoteViews.setViewVisibility(R.id.timeLeftIcon, View.VISIBLE)
                }
                remoteViews.setProgressBar(R.id.timeLeftBar, 100, remainingTime.second, false)

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

    private fun getRemainingTime(dayStartTime: Time, dayEndTime: Time): Pair<String?, Int> {
        val now = Calendar.getInstance()

        val startTimeCalendar = dayStartTime.toCalendar()
        val endTimeCalendar = dayEndTime.toCalendar()
        // Check if current time is after dayEndTime or before dayStartTime
        if (now.before(startTimeCalendar) && now.after(endTimeCalendar)) {
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
        val percentageLeft = (diff/totalTimeDiff * 100).toInt()
        return Pair(String.format("%02dh:%02dm", hours, minutes), percentageLeft)
    }

}
