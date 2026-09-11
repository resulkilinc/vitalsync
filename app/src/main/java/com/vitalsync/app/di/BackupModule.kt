package com.vitalsync.app.di

import com.vitalsync.app.data.db.dao.BloodPressureDao
import com.vitalsync.app.data.db.dao.GlucoseDao
import com.vitalsync.app.data.db.dao.HeartRateDao
import com.vitalsync.app.data.db.dao.OxygenDao
import com.vitalsync.app.data.db.dao.ReminderDao
import com.vitalsync.app.data.db.dao.UserDao
import com.vitalsync.app.util.DataBackupManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {

    @Provides
    @Singleton
    fun provideDataBackupManager(
        userDao: UserDao,
        bpDao: BloodPressureDao,
        glucoseDao: GlucoseDao,
        heartRateDao: HeartRateDao,
        oxygenDao: OxygenDao,
        reminderDao: ReminderDao,
    ): DataBackupManager =
        DataBackupManager(userDao, bpDao, glucoseDao, heartRateDao, oxygenDao, reminderDao)
}
