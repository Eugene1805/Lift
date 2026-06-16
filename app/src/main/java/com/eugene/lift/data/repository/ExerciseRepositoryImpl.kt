package com.eugene.lift.data.repository

import android.content.Context
import com.eugene.lift.common.localization.createLocalizedContext
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.SettingsDataSource
import com.eugene.lift.data.local.SeedExerciseStrings
import com.eugene.lift.data.mapper.toCrossRefs
import com.eugene.lift.data.mapper.toDomain
import com.eugene.lift.data.mapper.toEntity
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.repository.ExerciseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao,
    private val settingsDataSource: SettingsDataSource,
    @ApplicationContext private val context: Context
) : ExerciseRepository {

    override fun getExercises(): Flow<List<Exercise>> {
        return dao.getAllExercises().combine(languageCodeFlow()) { list, languageCode ->
            val localizedContext = context.createLocalizedContext(languageCode)
            list.map { SeedExerciseStrings.localize(localizedContext, it.toDomain()) }
        }
    }

    override fun getExerciseById(id: String): Flow<Exercise?> {
        return dao.getExerciseById(id).combine(languageCodeFlow()) { result, languageCode ->
            val localizedContext = context.createLocalizedContext(languageCode)
            result?.toDomain()?.let { SeedExerciseStrings.localize(localizedContext, it) }
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

    private fun languageCodeFlow(): Flow<String> {
        return settingsDataSource.userSettings
            .map { it.languageCode }
            .distinctUntilChanged()
    }
}
