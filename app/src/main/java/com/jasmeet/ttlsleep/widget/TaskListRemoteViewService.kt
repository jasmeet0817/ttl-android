package com.jasmeet.ttlsleep.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.room.Room
import com.jasmeet.ttlsleep.R
import com.jasmeet.ttlsleep.database.AppDatabase
import com.jasmeet.ttlsleep.database.Tasks
import com.jasmeet.ttlsleep.tasks_db_key
import com.jasmeet.ttlsleep.ttl_widget_db_name
import kotlinx.coroutines.runBlocking


internal class TaskListRemoteViewService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TaskListRemoteViewsFactory(this.applicationContext)
    }
}


class TaskListRemoteViewsFactory(val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var tasks : Tasks? = null

    private suspend fun getTasks(context: Context): Tasks? {
        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, ttl_widget_db_name
        ).build()

        val tasksDao = db.tasksDao()
        val tasks = tasksDao.getTasks(tasks_db_key) ?: return null
        return Tasks.fromString(tasks.serializedTasks)
    }

    override fun onCreate() {
        runBlocking {
            tasks = getTasks(context)
        }
    }

    override fun onDataSetChanged() {
        runBlocking {
            tasks = getTasks(context)
        }
    }

    override fun onDestroy() {
        TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        return tasks?.size() ?: 0
    }

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_task_list)
        if (tasks != null) {
            rv.setTextViewText(R.id.task_item_text, tasks!!.getTask(position))

            val fillInIntent = Intent()
            rv.setOnClickFillInIntent(R.id.task_item_text, fillInIntent)

        }
            // Handle item click intents here if necessary
        return rv
    }

    override fun getLoadingView(): RemoteViews? {
        // You can return null here if you don't need a custom loading view
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
