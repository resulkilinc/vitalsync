package com.vitalsync.app.data.repository

import com.vitalsync.app.data.db.dao.ReminderDao
import com.vitalsync.app.data.db.entities.ReminderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao
) {
    fun getAllReminders(userId: Long): Flow<List<ReminderEntity>> =
        reminderDao.getAllByUser(userId)

    suspend fun getEnabledReminders(userId: Long): List<ReminderEntity> =
        reminderDao.getEnabledReminders(userId)

    suspend fun getReminderById(id: Long): ReminderEntity? =
        reminderDao.getById(id)

    suspend fun insertReminder(
        userId: Long,
        title: String,
        description: String,
        type: String,
        hour: Int,
        minute: Int,
        daysOfWeek: String = "1,2,3,4,5,6,7"
    ): Long = reminderDao.insert(
        ReminderEntity(
            userId = userId,
            title = title,
            description = description,
            type = type,
            hour = hour,
            minute = minute,
            daysOfWeek = daysOfWeek
        )
    )

    suspend fun updateReminder(reminder: ReminderEntity) =
        reminderDao.update(reminder)

    suspend fun deleteReminder(reminder: ReminderEntity) =
        reminderDao.delete(reminder)

    suspend fun toggleReminder(id: Long, enabled: Boolean) =
        reminderDao.setEnabled(id, enabled)
}
