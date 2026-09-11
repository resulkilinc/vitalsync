package com.vitalsync.app.di

import android.content.Context
import androidx.room.Room
import com.vitalsync.app.data.db.AppDatabase
import com.vitalsync.app.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vitalsync_database",
        )
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideBloodPressureDao(db: AppDatabase): BloodPressureDao = db.bloodPressureDao()
    @Provides fun provideGlucoseDao(db: AppDatabase): GlucoseDao = db.glucoseDao()
    @Provides fun provideHeartRateDao(db: AppDatabase): HeartRateDao = db.heartRateDao()
    @Provides fun provideOxygenDao(db: AppDatabase): OxygenDao = db.oxygenDao()
    @Provides fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()
    @Provides fun provideSyncOutboxDao(db: AppDatabase): SyncOutboxDao = db.syncOutboxDao()
    @Provides fun provideSyncMetaDao(db: AppDatabase): SyncMetaDao = db.syncMetaDao()
}
