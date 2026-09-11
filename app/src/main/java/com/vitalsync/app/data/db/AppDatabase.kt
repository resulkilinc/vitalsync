package com.vitalsync.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vitalsync.app.data.db.dao.*
import com.vitalsync.app.data.db.entities.*

@Database(
    entities = [
        UserEntity::class,
        BloodPressureEntity::class,
        GlucoseEntity::class,
        HeartRateEntity::class,
        OxygenEntity::class,
        ReminderEntity::class,
        SyncOutboxEntity::class,
        SyncMetaEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun glucoseDao(): GlucoseDao
    abstract fun heartRateDao(): HeartRateDao
    abstract fun oxygenDao(): OxygenDao
    abstract fun reminderDao(): ReminderDao
    abstract fun syncOutboxDao(): SyncOutboxDao
    abstract fun syncMetaDao(): SyncMetaDao

    companion object {
        /** v3 → v4: uzaktan senkron kuyruğu ve meta tablosu */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sync_outbox` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `userId` INTEGER NOT NULL,
                      `vitalKind` TEXT NOT NULL,
                      `localRecordId` INTEGER NOT NULL,
                      `payloadJson` TEXT NOT NULL,
                      `status` TEXT NOT NULL,
                      `attempts` INTEGER NOT NULL,
                      `lastError` TEXT,
                      `createdAt` INTEGER NOT NULL,
                      `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_sync_outbox_status` ON `sync_outbox` (`status`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_sync_outbox_createdAt` ON `sync_outbox` (`createdAt`)",
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sync_meta` (
                      `key` TEXT NOT NULL PRIMARY KEY,
                      `value` TEXT NOT NULL,
                      `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
