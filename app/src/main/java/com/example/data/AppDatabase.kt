package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ConnectionLogDao
import com.example.data.dao.MirrorProfileDao
import com.example.data.entity.ConnectionLog
import com.example.data.entity.MirrorProfile

@Database(
    entities = [MirrorProfile::class, ConnectionLog::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mirrorProfileDao(): MirrorProfileDao
    abstract fun connectionLogDao(): ConnectionLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scrcpy_mirror_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
