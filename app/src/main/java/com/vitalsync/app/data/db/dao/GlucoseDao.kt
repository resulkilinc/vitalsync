package com.vitalsync.app.data.db.dao

import androidx.room.*
import com.vitalsync.app.data.db.entities.GlucoseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {
    @Insert
    suspend fun insert(glucose: GlucoseEntity): Long

    @Update
    suspend fun update(glucose: GlucoseEntity)

    @Delete
    suspend fun delete(glucose: GlucoseEntity)

    @Query("SELECT * FROM glucose WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllByUser(userId: Long): Flow<List<GlucoseEntity>>

    @Query("SELECT * FROM glucose WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getByDateRange(userId: Long, startTime: Long, endTime: Long): List<GlucoseEntity>

    @Query("SELECT * FROM glucose WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(userId: Long): GlucoseEntity?
}
