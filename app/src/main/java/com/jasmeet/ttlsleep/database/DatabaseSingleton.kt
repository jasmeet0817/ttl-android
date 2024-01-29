package com.jasmeet.ttlsleep.database

import android.content.Context
import androidx.room.Room
import com.jasmeet.ttlsleep.ttl_widget_db_name
import javax.inject.Singleton

@Singleton
class DatabaseSingleton private constructor(context: Context) {
    private val database: AppDatabase

    init {
        // Initialize your Room database here
        database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, ttl_widget_db_name
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    companion object {
        @Volatile
        private var instance: DatabaseSingleton? = null

        fun getInstance(context: Context): DatabaseSingleton {
            if (instance == null) {
                synchronized(DatabaseSingleton::class) {
                    instance = DatabaseSingleton(context)
                }
            }
            return instance!!
        }
    }

    fun getAppDatabase(): AppDatabase {
        return database
    }
}