package com.vitalsync.app.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class VitalsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result =
        when (syncRepository.flushPending(batchSize = 20)) {
            SyncFlushOutcome.Success -> Result.success()
            SyncFlushOutcome.Retry -> Result.retry()
        }
}
