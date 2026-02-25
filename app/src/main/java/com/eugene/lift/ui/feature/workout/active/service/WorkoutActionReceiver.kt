package com.eugene.lift.ui.feature.workout.active.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var serviceManager: ActiveWorkoutServiceManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_COMPLETE_SET) {
            serviceManager.dispatchAction(WorkoutNotificationAction.CompleteCurrentSet)
        }
    }

    companion object {
        const val ACTION_COMPLETE_SET = "com.eugene.lift.ACTION_COMPLETE_SET"
    }
}
