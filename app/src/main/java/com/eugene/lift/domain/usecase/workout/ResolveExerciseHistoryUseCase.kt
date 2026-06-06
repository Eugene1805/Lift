package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.WorkoutRepository
import javax.inject.Inject

data class ExerciseHistorySnapshot(
    val displaySets: List<WorkoutSet> = emptyList(),
    val prefillSets: List<WorkoutSet> = emptyList()
)

class ResolveExerciseHistoryUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(exerciseId: String, templateId: String?): ExerciseHistorySnapshot {
        val displaySession = workoutRepository.getLastHistoryForExercise(exerciseId, null)
        val prefillSession = if (templateId == null) {
            displaySession
        } else {
            workoutRepository.getLastHistoryForExercise(exerciseId, templateId)
        }

        return ExerciseHistorySnapshot(
            displaySets = displaySession.exerciseSetsFor(exerciseId),
            prefillSets = prefillSession.exerciseSetsFor(exerciseId)
        )
    }
}

private fun com.eugene.lift.domain.model.WorkoutSession?.exerciseSetsFor(
    exerciseId: String
): List<WorkoutSet> {
    return this?.exercises
        ?.firstOrNull { sessionExercise -> sessionExercise.exercise.id == exerciseId }
        ?.sets
        .orEmpty()
}
