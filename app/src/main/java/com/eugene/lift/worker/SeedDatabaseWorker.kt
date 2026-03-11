package com.eugene.lift.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eugene.lift.data.local.ExerciseSeeder
import com.eugene.lift.domain.usecase.exercise.AssignMissingImagesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SeedDatabaseWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val seeder: ExerciseSeeder,
    private val assignMissingImagesUseCase: AssignMissingImagesUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            seeder.populate()
            assignMissingImagesUseCase()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}