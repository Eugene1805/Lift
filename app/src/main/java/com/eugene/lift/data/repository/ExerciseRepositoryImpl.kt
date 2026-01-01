package com.eugene.lift.data.repository

import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.mapper.toCrossRefs
import com.eugene.lift.data.mapper.toDomain
import com.eugene.lift.data.mapper.toEntity
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao
) : ExerciseRepository {

    override fun getExercises(): Flow<List<Exercise>> {
        return dao.getAllExercises().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getExerciseById(id: String): Flow<Exercise?> {
        return dao.getExerciseById(id).map { it?.toDomain() }
    }

    override suspend fun saveExercise(exercise: Exercise) {
        dao.saveExerciseComplete(
            exercise = exercise.toEntity(),
            refs = exercise.toCrossRefs()
        )
    }

    override suspend fun deleteExercise(exerciseId: String) {
        dao.deleteExerciseComplete(exerciseId)
    }

    override suspend fun getCount(): Int {
        return dao.getExerciseCount()
    }
}