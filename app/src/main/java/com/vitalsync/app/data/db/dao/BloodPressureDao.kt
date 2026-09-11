package com.vitalsync.app.data.db.dao

import androidx.room.*
import com.vitalsync.app.data.db.entities.BloodPressureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureDao {
    @Insert
    suspend fun insert(bp: BloodPressureEntity): Long

    @Update
    suspend fun update(bp: BloodPressureEntity)

    @Delete
    suspend fun delete(bp: BloodPressureEntity)

    @Query("SELECT * FROM blood_pressure WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllByUser(userId: Long): Flow<List<BloodPressureEntity>>

    @Query("SELECT * FROM blood_pressure WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getByDateRange(userId: Long, startTime: Long, endTime: Long): List<BloodPressureEntity>

    @Query("SELECT * FROM blood_pressure WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(userId: Long): BloodPressureEntity?
}
