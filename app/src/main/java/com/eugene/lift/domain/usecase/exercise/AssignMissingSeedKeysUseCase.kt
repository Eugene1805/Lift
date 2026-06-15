package com.eugene.lift.domain.usecase.exercise

import android.content.Context
import com.eugene.lift.data.local.SeedExerciseStrings
import com.eugene.lift.domain.repository.ExerciseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AssignMissingSeedKeysUseCase @Inject constructor(
    private val repository: ExerciseRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke() {
        repository.getExercisesWithoutSeedKey().forEach { exercise ->
            val seedKey = SeedExerciseStrings.findSeedKeyByName(context, exercise.name) ?: return@forEach
            repository.updateSeedKey(exercise.id, seedKey)
        }
    }
}
