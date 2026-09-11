package com.vitalsync.app.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val workManager get() = WorkManager.getInstance(context)

    /** Periyodik arka plan senkronu — internet geldiğinde outbox boşaltılır */
    fun schedulePeriodicMaintenance() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodic = PeriodicWorkRequestBuilder<VitalsSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncConstants.WORK_UNIQUE_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            periodic,
        )
    }

    /** Tek seferlik senkron — yeni ölçüm kaydı sonrası veya kullanıcı tetiklemesi */
    fun enqueueImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val once = OneTimeWorkRequestBuilder<VitalsSyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            SyncConstants.WORK_UNIQUE_ONCE,
            ExistingWorkPolicy.REPLACE,
            once,
        )
    }
}
