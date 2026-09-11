package com.vitalsync.app.data.db.dao

import androidx.room.*
import com.vitalsync.app.data.db.entities.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY hour ASC, minute ASC")
    fun getAllByUser(userId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isEnabled = 1")
    suspend fun getEnabledReminders(userId: Long): List<ReminderEntity>

    @Query("UPDATE reminders SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
