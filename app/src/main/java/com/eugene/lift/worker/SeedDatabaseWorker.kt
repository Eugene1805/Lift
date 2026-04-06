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
            // First, ensure the base exercise data is present in the database.
            seeder.populate()
            
            // Then, trigger the image assignment process. This is executed separately
            // to handle exercises that may have been added in versions where image
            // paths were not yet part of the seed data or to repair missing links.
            assignMissingImagesUseCase()
            
            Result.success()
        } catch (e: Exception) {
            // Log full stack trace to aid in debugging production seeding failures.
            e.printStackTrace()
            Result.failure()
        }
    }
}