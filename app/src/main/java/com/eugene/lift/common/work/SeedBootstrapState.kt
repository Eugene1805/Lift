package com.eugene.lift.common.work

import android.content.Context

internal object SeedBootstrapState {

    private const val PREFS_NAME = "seed_bootstrap_state"
    private const val KEY_COMPLETED_WORK_NAME = "completed_work_name"

    fun shouldEnqueue(context: Context, workName: String): Boolean {
        return completedWorkName(context) != workName
    }

    fun markCompleted(context: Context, workName: String): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_COMPLETED_WORK_NAME, workName)
            .commit()
    }

    private fun completedWorkName(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_COMPLETED_WORK_NAME, null)
    }
}
