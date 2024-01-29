package com.jasmeet.ttlsleep.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity
data class TasksDbEntity(@PrimaryKey val id: String, val serializedTasks: String)

@Dao
interface TasksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(tasks: TasksDbEntity)

    @Query("SELECT * FROM TasksDbEntity WHERE id = :id")
    suspend fun getTasks(id: String): TasksDbEntity?
}