package com.jasmeet.ttlsleep.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity
data class TimeDbEntity(@PrimaryKey val id: String, val timeString: String)

@Dao
interface TimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(time: TimeDbEntity)

    @Query("SELECT * FROM TimeDbEntity WHERE id = :id")
    suspend fun getTime(id: String): TimeDbEntity?
}