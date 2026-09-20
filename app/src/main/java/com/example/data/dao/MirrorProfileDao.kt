package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.MirrorProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface MirrorProfileDao {
    @Query("SELECT * FROM mirror_profiles ORDER BY isDefault DESC, id ASC")
    fun getAllProfiles(): Flow<List<MirrorProfile>>

    @Query("SELECT * FROM mirror_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): MirrorProfile?

    @Query("SELECT * FROM mirror_profiles WHERE isDefault = 1 LIMIT 1")
    fun getDefaultProfile(): Flow<MirrorProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: MirrorProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<MirrorProfile>)

    @Update
    suspend fun updateProfile(profile: MirrorProfile)

    @Delete
    suspend fun deleteProfile(profile: MirrorProfile)

    @Query("UPDATE mirror_profiles SET isDefault = 0")
    suspend fun clearDefaultFlags()

    @Query("UPDATE mirror_profiles SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultProfile(id: Long)

    @Query("SELECT COUNT(*) FROM mirror_profiles")
    suspend fun getProfileCount(): Int
}
