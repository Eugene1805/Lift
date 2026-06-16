package com.eugene.lift.common.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.eugene.lift.worker.ActiveWorkoutReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveWorkoutReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedule() {
        val request = OneTimeWorkRequestBuilder<ActiveWorkoutReminderWorker>()
            .setInitialDelay(REMINDER_DELAY_MINUTES, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            REMINDER_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    companion object {
        const val REMINDER_WORK_NAME = "active_workout_reminder"
        const val REMINDER_DELAY_MINUTES = 30L
    }
}
