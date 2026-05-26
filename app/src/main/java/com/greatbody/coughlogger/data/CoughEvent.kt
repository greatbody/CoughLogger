package com.greatbody.coughlogger.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cough_events")
data class CoughEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "score") val score: Float,
    @ColumnInfo(name = "label") val label: String = "Cough"
)
