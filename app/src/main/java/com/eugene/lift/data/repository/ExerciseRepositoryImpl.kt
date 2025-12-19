package com.eugene.lift.data.repository

import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao
) : ExerciseRepository {

    override fun getExercises(): Flow<List<ExerciseEntity>> = dao.getAllExercises()

    override suspend fun getExercise(id: String): ExerciseEntity? = dao.getExerciseById(id)

    override suspend fun saveExercise(exercise: ExerciseEntity) = dao.insertExercise(exercise)

    override suspend fun deleteExercise(exercise: ExerciseEntity) = dao.deleteExercise(exercise)
}