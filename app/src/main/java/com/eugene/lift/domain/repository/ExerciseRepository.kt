package com.eugene.lift.domain.repository

import com.eugene.lift.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getExercises(): Flow<List<Exercise>>
    fun getExerciseById(id: String): Flow<Exercise?>
    suspend fun saveExercise(exercise: Exercise)
    suspend fun deleteExercise(exerciseId: String)
    suspend fun getCount(): Int

}