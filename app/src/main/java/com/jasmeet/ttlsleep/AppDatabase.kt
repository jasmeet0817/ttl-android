package com.jasmeet.ttlsleep

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TimeDbEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timeDao(): TimeDao
}
