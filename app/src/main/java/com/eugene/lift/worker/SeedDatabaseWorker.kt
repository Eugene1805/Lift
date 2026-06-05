package com.eugene.lift.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eugene.lift.data.local.ExerciseBootstrapDataSource
import com.eugene.lift.domain.usecase.exercise.AssignMissingImagesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SeedDatabaseWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val bootstrapDataSource: ExerciseBootstrapDataSource,
    private val assignMissingImagesUseCase: AssignMissingImagesUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            bootstrapDataSource.populateIfEmpty()
            assignMissingImagesUseCase()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
