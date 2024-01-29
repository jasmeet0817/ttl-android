package com.jasmeet.ttlsleep.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TimeDbEntity::class, TasksDbEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timeDao(): TimeDao
    abstract fun tasksDao(): TasksDao
}
