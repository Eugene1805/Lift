package com.eugene.lift.common.work

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.eugene.lift.worker.SeedDatabaseWorker
import com.eugene.lift.worker.SyncExercisesWorker

/**
 * Utility object for initializing work tasks in the app.
 */
object WorkInitializer {

    private const val TAG = "WorkInitializer"
    private const val SEED_DB_WORK_NAME = "seed_db_work"
    private const val SYNC_EXERCISES_WORK_NAME = "sync_exercises_work"

    /**
     * Enqueues the database seeding work if it hasn't been done yet.
     *
     * Uses ExistingWorkPolicy.KEEP to ensure the work is only done once.
     *
     * @param context Application context
     */
    fun enqueueDatabaseSeeding(context: Context) {
        Log.d(TAG, "Enqueueing database seeding work")

        val seedRequest = OneTimeWorkRequest.Builder(SeedDatabaseWorker::class.java)
            .build()
        val syncRequest = OneTimeWorkRequest.Builder(SyncExercisesWorker::class.java)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SEED_DB_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            seedRequest
        )
        WorkManager.getInstance(context).enqueueUniqueWork(
            SYNC_EXERCISES_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            syncRequest
        )

        Log.i(TAG, "Exercise bootstrap and sync work enqueued successfully")
    }
}
