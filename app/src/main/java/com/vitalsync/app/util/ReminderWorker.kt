package com.vitalsync.app.util

import android.content.Context
import androidx.work.*
import com.vitalsync.app.data.db.dao.ReminderDao
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * WorkManager Worker — planlanmış hatırlatıcı bildirimlerini tetikler.
 */
class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "VitalÖlçüm Hatırlatıcı"
        val message = inputData.getString("message") ?: "Sağlık ölçümünüzü yapmayı unutmayın!"
        val reminderId = inputData.getLong("reminderId", 0)

        NotificationHelper.showReminderNotification(
            context = context,
            reminderId = reminderId,
            title = title,
            message = message
        )

        return Result.success()
    }

    companion object {
        /**
         * Belirli bir saat/dakika için tekrarlayan hatırlatıcı planlar.
         */
        fun scheduleReminder(
            context: Context,
            reminderId: Long,
            title: String,
            description: String,
            hour: Int,
            minute: Int
        ) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // Eğer hedef zaman geçmişse yarına al
                if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
            }

            val delayMs = target.timeInMillis - now.timeInMillis

            val inputData = Data.Builder()
                .putLong("reminderId", reminderId)
                .putString("title", title)
                .putString("message", description)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("reminder_$reminderId")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "reminder_$reminderId",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
        }

        /**
         * Belirli bir hatırlatıcıyı iptal eder.
         */
        fun cancelReminder(context: Context, reminderId: Long) {
            WorkManager.getInstance(context)
                .cancelUniqueWork("reminder_$reminderId")
        }

        /**
         * Tüm hatırlatıcıları iptal eder.
         */
        fun cancelAllReminders(context: Context) {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag("reminder")
        }
    }
}
