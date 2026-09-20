package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ConnectionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionLogDao {
    @Query("SELECT * FROM connection_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<ConnectionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ConnectionLog): Long

    @Query("DELETE FROM connection_logs")
    suspend fun clearAllLogs()
}
