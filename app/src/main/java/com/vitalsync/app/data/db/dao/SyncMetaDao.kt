package com.vitalsync.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vitalsync.app.data.db.entities.SyncMetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncMetaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meta: SyncMetaEntity)

    @Query("SELECT * FROM sync_meta WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): SyncMetaEntity?

    @Query("SELECT * FROM sync_meta WHERE `key` = :key LIMIT 1")
    fun observe(key: String): Flow<SyncMetaEntity?>
}
