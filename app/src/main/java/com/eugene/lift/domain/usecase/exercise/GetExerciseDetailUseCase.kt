package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExerciseDetailUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    operator fun invoke(id: String): Flow<Exercise?> {
        return repository.getExerciseById(id)
    }
}