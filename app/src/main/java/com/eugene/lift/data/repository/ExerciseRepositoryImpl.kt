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

    override suspend fun getExercisesWithoutImage(): List<Exercise> {
        return dao.getExercisesWithoutImage().map { entity ->
            // We provide an empty list for bodyParts here because the image mapping
            // logic only requires the exercise ID and name. This avoids the
            // performance cost of performing a multi-table join for this specific
            // background operation.
            Exercise(
                id = entity.id,
                name = entity.name,
                category = entity.category,
                measureType = entity.measureType,
                instructions = entity.instructions,
                imagePath = entity.imagePath,
                bodyParts = emptyList()
            )
        }
    }

    override suspend fun updateImagePath(exerciseId: String, imagePath: String) {
        // Leverages the targeted DAO update to minimize write amplification.
        dao.updateImagePath(exerciseId, imagePath)
    }
}