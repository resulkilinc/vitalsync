package com.vitalsync.app.data.db.dao

import androidx.room.*
import com.vitalsync.app.data.db.entities.HeartRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateDao {
    @Insert
    suspend fun insert(hr: HeartRateEntity): Long

    @Update
    suspend fun update(hr: HeartRateEntity)

    @Delete
    suspend fun delete(hr: HeartRateEntity)

    @Query("SELECT * FROM heart_rate WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllByUser(userId: Long): Flow<List<HeartRateEntity>>

    @Query("SELECT * FROM heart_rate WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getByDateRange(userId: Long, startTime: Long, endTime: Long): List<HeartRateEntity>

    @Query("SELECT * FROM heart_rate WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(userId: Long): HeartRateEntity?
}
