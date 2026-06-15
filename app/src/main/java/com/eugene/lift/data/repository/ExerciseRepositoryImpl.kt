package com.eugene.lift.data.repository

import android.content.Context
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.SeedExerciseStrings
import com.eugene.lift.data.mapper.toCrossRefs
import com.eugene.lift.data.mapper.toDomain
import com.eugene.lift.data.mapper.toEntity
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.repository.ExerciseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao,
    @ApplicationContext private val context: Context
) : ExerciseRepository {

    override fun getExercises(): Flow<List<Exercise>> {
        return dao.getAllExercises().map { list ->
            list.map { SeedExerciseStrings.localize(context, it.toDomain()) }
        }
    }

    override fun getExerciseById(id: String): Flow<Exercise?> {
        return dao.getExerciseById(id).map { result ->
            result?.toDomain()?.let { SeedExerciseStrings.localize(context, it) }
        }
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
            SeedExerciseStrings.localize(context, Exercise(
                id = entity.id,
                name = entity.name,
                category = entity.category,
                measureType = entity.measureType,
                instructions = entity.instructions,
                imagePath = entity.imagePath,
                bodyParts = emptyList(),
                seedKey = entity.seedKey
            ))
        }
    }

    override suspend fun getExercisesWithoutSeedKey(): List<Exercise> {
        return dao.getExercisesWithoutSeedKey().map { entity ->
            Exercise(
                id = entity.id,
                name = entity.name,
                category = entity.category,
                measureType = entity.measureType,
                instructions = entity.instructions,
                imagePath = entity.imagePath,
                bodyParts = emptyList(),
                seedKey = entity.seedKey
            )
        }
    }

    override suspend fun updateImagePath(exerciseId: String, imagePath: String) {
        dao.updateImagePath(exerciseId, imagePath)
    }

    override suspend fun updateSeedKey(exerciseId: String, seedKey: String) {
        dao.updateSeedKey(exerciseId, seedKey)
    }
}
