package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.TemplateRepository
import com.eugene.lift.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class StartWorkoutFromTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val workoutRepository: WorkoutRepository
) {
    /**
     * Crea una nueva sesión activa basada en una plantilla.
     * Pre-crea los sets vacíos según el 'targetSets' de la plantilla.
     */
    suspend operator fun invoke(templateId: String): WorkoutSession? {
        val template = templateRepository.getTemplate(templateId).first() ?: return null
        val lastSetsByExerciseId = loadLastSetsByExerciseId(template)

        val sessionExercises = template.exercises.map { templateExercise ->
            val historicalSets = lastSetsByExerciseId[templateExercise.exercise.id].orEmpty()

            val initialSets = (0 until templateExercise.targetSets).map { setIndex ->
                WorkoutSet(
                    id = UUID.randomUUID().toString(),
                    weight = historicalSets.getOrNull(setIndex)?.weight ?: 0.0,
                    reps = 0,
                    completed = false,
                    rpe = null,
                    rir = null,
                    isPr = false
                )
            }

            SessionExercise(
                id = UUID.randomUUID().toString(),
                exercise = templateExercise.exercise,
                sets = initialSets
            )
        }

        return WorkoutSession(
            id = UUID.randomUUID().toString(),
            templateId = template.id,
            name = template.name,
            date = LocalDateTime.now(),
            durationSeconds = 0,
            exercises = sessionExercises
        )
    }

    private suspend fun loadLastSetsByExerciseId(
        template: com.eugene.lift.domain.model.WorkoutTemplate
    ): Map<String, List<WorkoutSet>> {
        return template.exercises
            .map { it.exercise.id }
            .distinct()
            .associateWith { exerciseId ->
                val lastSession = workoutRepository.getLastHistoryForExercise(exerciseId, template.id)
                lastSession
                    ?.exercises
                    ?.firstOrNull { it.exercise.id == exerciseId }
                    ?.sets
                    .orEmpty()
            }
    }
}