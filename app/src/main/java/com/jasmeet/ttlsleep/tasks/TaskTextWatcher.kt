package com.jasmeet.ttlsleep.tasks

import android.text.Editable
import android.text.TextWatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TaskTextWatcher(private val tasksManager: TasksManager) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(text: Editable?) {
        if (text == null) return
        GlobalScope.launch {
            tasksManager.addTasksToDb()
        }
        if (text.isEmpty()) return
        tasksManager.materializeNextTextBoxIfNeeded()
    }
}
