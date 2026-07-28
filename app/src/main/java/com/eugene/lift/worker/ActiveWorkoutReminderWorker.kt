package com.eugene.lift.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eugene.lift.MainActivity
import com.eugene.lift.R
import com.eugene.lift.common.work.ActiveWorkoutReminderScheduler
import com.eugene.lift.domain.repository.ActiveWorkoutDraftRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class ActiveWorkoutReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val activeWorkoutDraftRepository: ActiveWorkoutDraftRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val draft = activeWorkoutDraftRepository.getDraft() ?: return Result.success()
        val inactivityMillis = System.currentTimeMillis() - draft.lastInteractedAtEpochMillis
        val requiredMillis = TimeUnit.MINUTES.toMillis(ActiveWorkoutReminderScheduler.REMINDER_DELAY_MINUTES)

        if (inactivityMillis < requiredMillis) {
            return Result.success()
        }

        createNotificationChannel()
        if (canPostNotifications()) {
            try {
                NotificationManagerCompat.from(applicationContext).notify(
                    NOTIFICATION_ID,
                    buildNotification(draft.toSummary().sessionName)
                )
            } catch (_: SecurityException) {
                // Permission can be revoked between the check and notify call.
            }
        }

        return Result.success()
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildNotification(sessionName: String): android.app.Notification {
        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(R.string.workout_draft_notification_title))
            .setContentText(
                applicationContext.getString(
                    R.string.workout_draft_notification_message,
                    sessionName
                )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            applicationContext.getString(R.string.workout_draft_notification_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = applicationContext.getString(R.string.workout_draft_notification_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "active_workout_draft_channel"
        private const val NOTIFICATION_ID = 2001
    }
}
