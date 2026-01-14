package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.repository.WorkoutRepository
import javax.inject.Inject

class GetLastHistoryForExerciseUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    /**
     * Busca la última sesión donde se realizó este ejercicio para mostrar "Ghost Data".
     */
    suspend operator fun invoke(exerciseId: String): WorkoutSession? {
        return repository.getLastHistoryForExercise(exerciseId)
    }
}