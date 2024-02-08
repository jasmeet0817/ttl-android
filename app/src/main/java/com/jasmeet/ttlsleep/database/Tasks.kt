package com.jasmeet.ttlsleep.database

private const val tasksDelimiter = "-----"

class Tasks {
    private var tasks: MutableList<String> = mutableListOf()

    fun addTask(item: String) {
        tasks.add(item)
    }

    fun getTasks(maxTasks: Int = Int.MAX_VALUE): List<String> {
        val size = tasks.size
        return if (maxTasks >= size) {
            tasks
        } else {
            tasks.subList(size - maxTasks, size)
        }
    }

    fun getTask(index: Int): String {
        return tasks[index]
    }

    fun size(): Int {
        return tasks.size
    }

    override fun toString(): String {
        return tasks.joinToString(separator = tasksDelimiter)
    }


    companion object {
        fun fromString(data: String): Tasks? {
            val items = if (data.isNotEmpty()) data.split(tasksDelimiter) else listOf()

            val tasks = Tasks()
            items.forEach { item ->
                if (item.isNotEmpty()) {
                    tasks.addTask(item)
                }
            }
            return tasks
        }
    }
}