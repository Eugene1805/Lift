package com.eugene.lift.ui.feature.workout.active.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.eugene.lift.MainActivity
import com.eugene.lift.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class ActiveWorkoutService : Service() {

    @Inject
    lateinit var serviceManager: ActiveWorkoutServiceManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, buildNotification(null), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, buildNotification(null))
        }

        serviceManager.notificationState
            .onEach { state ->
                notificationManager.notify(NOTIFICATION_ID, buildNotification(state))
            }
            .launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(state: WorkoutNotificationState?): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Using launcher icon temporarily
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (state == null) {
            builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.active_workout_summary))
        } else {
            builder.setContentTitle(state.exerciseName)
            
            val weightStr = if (state.isBodyweight) {
                getString(R.string.cat_bodyweight)
            } else if (state.weight > 0) {
                val formattedWeight = if (state.weight % 1.0 == 0.0) state.weight.toInt().toString() else state.weight.toString()
                "$formattedWeight ${state.weightUnitLabel}"
            } else {
                ""
            }
            
            val repsStr = if (state.reps > 0) "${state.reps} reps" else ""
            
            val details = listOf(weightStr, repsStr).filter { it.isNotEmpty() }.joinToString(" × ")
            val setPrefix = getString(R.string.active_workout_set) + " ${state.setNumber}"
            
            val contentText = if (details.isNotEmpty()) "$setPrefix: $details" else setPrefix
            builder.setContentText(contentText)

            val completeIntent = Intent(this, WorkoutActionReceiver::class.java).apply {
                action = WorkoutActionReceiver.ACTION_COMPLETE_SET
            }
            val pendingComplete = PendingIntent.getBroadcast(
                this, 1, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            builder.addAction(
                0,
                getString(R.string.action_complete_set),
                pendingComplete
            )
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_workout),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_workout_desc)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "active_workout_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_SERVICE = "com.eugene.lift.ACTION_STOP_SERVICE"
    }
}
