package com.eugene.lift.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eugene.lift.core.util.Logger
import com.eugene.lift.data.local.ExerciseBootstrapDataSource
import com.eugene.lift.data.remote.ExerciseCatalogSyncer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

@HiltWorker
class SyncExercisesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val bootstrapDataSource: ExerciseBootstrapDataSource,
    private val exerciseCatalogSyncer: ExerciseCatalogSyncer,
    private val logger: Logger
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            bootstrapDataSource.populateIfEmpty()
            exerciseCatalogSyncer.syncExercises()
            Result.success()
        } catch (exception: IOException) {
            logger.log(exception)
            Result.retry()
        } catch (exception: Exception) {
            logger.log(exception)
            Result.failure()
        }
    }
}
