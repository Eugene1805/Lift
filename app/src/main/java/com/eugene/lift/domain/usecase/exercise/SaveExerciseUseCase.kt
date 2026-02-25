package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.repository.ExerciseRepository
import javax.inject.Inject

class SaveExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository,
    private val safeExecutor: SafeExecutor
) {
    suspend operator fun invoke(exercise: Exercise): AppResult<Unit> {
        if (exercise.name.isBlank()) {
            return AppResult.Error(AppError.Validation)
        }

        return safeExecutor.execute {
            repository.saveExercise(exercise)
        }
    }
}