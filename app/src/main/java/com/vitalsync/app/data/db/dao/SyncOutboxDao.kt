package com.vitalsync.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vitalsync.app.data.db.entities.SyncOutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOutboxDao {

    @Insert
    suspend fun insert(row: SyncOutboxEntity): Long

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM sync_outbox WHERE status = 'PENDING' ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPending(limit: Int): List<SyncOutboxEntity>

    @Query("DELETE FROM sync_outbox WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        UPDATE sync_outbox
        SET attempts = :attempts,
            lastError = :error,
            status = :status,
            updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateAfterAttempt(
        id: Long,
        attempts: Int,
        error: String?,
        status: String,
        updatedAt: Long,
    )
}
