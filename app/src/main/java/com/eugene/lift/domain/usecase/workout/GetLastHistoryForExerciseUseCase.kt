package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.repository.WorkoutRepository
import javax.inject.Inject

class GetLastHistoryForExerciseUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    /**
     * Busca la última sesión donde se realizó este ejercicio para mostrar "Ghost Data".
     * Si se proporciona [templateId], sólo considera sesiones originadas desde esa plantilla.
     */
    suspend operator fun invoke(exerciseId: String, templateId: String? = null): WorkoutSession? {
        return repository.getLastHistoryForExercise(exerciseId, templateId)
    }
}