package com.eugene.lift.domain.repository

import com.eugene.lift.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getExercises(): Flow<List<ExerciseEntity>>
    suspend fun getExercise(id: String): ExerciseEntity?
    suspend fun saveExercise(exercise: ExerciseEntity)
    suspend fun deleteExercise(exercise: ExerciseEntity)
}