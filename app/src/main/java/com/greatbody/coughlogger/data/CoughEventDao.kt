package com.greatbody.coughlogger.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CoughEventDao {
    @Insert
    suspend fun insert(event: CoughEvent): Long

    @Query("SELECT * FROM cough_events ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 500): Flow<List<CoughEvent>>

    @Query("SELECT * FROM cough_events ORDER BY timestamp ASC")
    suspend fun getAll(): List<CoughEvent>

    @Query("SELECT COUNT(*) FROM cough_events WHERE timestamp >= :sinceMillis")
    fun countSince(sinceMillis: Long): Flow<Int>

    @Query("SELECT * FROM cough_events WHERE timestamp >= :startMillis AND timestamp < :endMillis ORDER BY timestamp ASC")
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<CoughEvent>>

    @Query("DELETE FROM cough_events")
    suspend fun deleteAll()
}
