package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_logs")
data class ConnectionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileName: String,
    val durationSeconds: Long,
    val totalBytesTransferred: Long,
    val averageFps: Int,
    val audioPacketsSent: Long,
    val connectionType: String = "USB (ADB)",
    val timestamp: Long = System.currentTimeMillis()
)
