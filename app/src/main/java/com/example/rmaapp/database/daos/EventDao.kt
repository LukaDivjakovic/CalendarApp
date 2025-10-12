package com.example.rmaapp.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.rmaapp.database.entities.Event
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface EventDao {

    @Insert
    suspend fun insert(event: Event)

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query("SELECT * FROM events WHERE id = :id")
    fun getEvent(id: Int): Flow<Event>

    @Query("SELECT * FROM events WHERE startTime >= :start AND endTime <= :end ORDER BY startTime ASC")
    fun getEventsForDateRange(start: LocalDateTime, end: LocalDateTime): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE startTime >= :startOfDay AND startTime < :endOfDay")
    suspend fun getEventsForDate(startOfDay: LocalDateTime, endOfDay: LocalDateTime): List<Event>
}