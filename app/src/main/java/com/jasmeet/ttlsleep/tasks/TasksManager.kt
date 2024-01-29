package com.jasmeet.ttlsleep.tasks

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jasmeet.ttlsleep.MainActivity
import com.jasmeet.ttlsleep.R
import com.jasmeet.ttlsleep.database.AppDatabase
import com.jasmeet.ttlsleep.database.Tasks
import com.jasmeet.ttlsleep.database.TasksDbEntity
import com.jasmeet.ttlsleep.tasks_db_key
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TasksManager(
    private val db: AppDatabase,
    private val activity: Activity
) {

    private val taskWatcher = TaskTextWatcher(this)

    fun setupTasks() {
        GlobalScope.launch {
            val tasksModel = getAllTasksFromDAO()
            val container = activity.findViewById<LinearLayout>(R.id.tasksContainer)
            val totalTasks = tasksModel.getTasks().size

            if (totalTasks == 0) {
                materializeNextTextBoxIfNeeded()
            }
            for (i in 0 until totalTasks) {
                val view = container.getChildAt(i)
                setupTaskBox(view, tasksModel.getTask(i))
            }
        }
    }

    private fun setupTaskBox(taskBox: View, text: String) {
        taskBox.visibility = View.VISIBLE
        val textInput = taskBox.findViewWithTag<TextInputEditText>("textInput")
        activity.runOnUiThread {
            textInput?.setText(text)
        }
        textInput?.addTextChangedListener(taskWatcher)
        (taskBox as TextInputLayout).setEndIconOnClickListener {
            textInput?.setText("")
            taskBox.visibility = View.GONE
        }
    }

    private suspend fun getAllTasksFromDAO(): Tasks {
        val tasksDao = db.tasksDao()
        val tasksDbEntity = tasksDao.getTasks(tasks_db_key) ?: return Tasks()
        return Tasks.fromString(tasksDbEntity.serializedTasks) ?: return Tasks()
    }

    // TODO: This function might be slow, optimize if needed.
    suspend fun addTasksToDb() {
        val tasks = getAllTasksFromInput(activity)
        val tasksDao = db.tasksDao()

        // Insert/Update entity
        val tasksEntity = TasksDbEntity(tasks_db_key, tasks.toString())
        tasksDao.update(tasksEntity)
        (activity as MainActivity).onUpdateWidgetMessage()
    }

    private fun getAllTasksFromInput(activity: Activity): Tasks {
        val tasks = Tasks()
        val container = activity.findViewById<LinearLayout>(R.id.tasksContainer)

        for (i in 0 until container.childCount) {
            val view = container.getChildAt(i)
            val textInput = view.findViewWithTag<TextInputEditText>("textInput")
            val text = textInput.text.toString()
            if (!text.isNullOrEmpty()) {
                tasks.addTask(text)
            }
        }
        return tasks
    }

    fun materializeNextTextBoxIfNeeded() {
        val container = activity.findViewById<LinearLayout>(R.id.tasksContainer)
        for (i in 0 until container.childCount) {
            val taskBox = container.getChildAt(i)
            if (taskBox.visibility == View.VISIBLE && taskBox.findViewWithTag<TextInputEditText>("textInput").text.isNullOrEmpty() ) {
                // There is already an empty visible task
                return
            }
            if (taskBox.visibility == View.GONE) {
                setupTaskBox(taskBox, "")
                return
            }
        }
    }
}