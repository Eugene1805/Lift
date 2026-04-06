package com.eugene.lift.domain.repository

import com.eugene.lift.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

/**
 * Interface that abstracts the data source for exercise-related information.
 */
interface ExerciseRepository {
    fun getExercises(): Flow<List<Exercise>>
    fun getExerciseById(id: String): Flow<Exercise?>
    suspend fun saveExercise(exercise: Exercise)
    suspend fun deleteExercise(exerciseId: String)
    suspend fun getCount(): Int

    /**
     * Retrieves a snapshot of exercises lacking a visual asset.
     */
    suspend fun getExercisesWithoutImage(): List<Exercise>

    /**
     * Efficiently updates the asset path without a full repository rewrite.
     */
    suspend fun updateImagePath(exerciseId: String, imagePath: String)
}