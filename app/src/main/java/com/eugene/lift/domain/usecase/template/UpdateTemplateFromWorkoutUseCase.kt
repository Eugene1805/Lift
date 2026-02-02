package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.model.TemplateExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

class UpdateTemplateFromWorkoutUseCase @Inject constructor(
    private val repository: TemplateRepository,
    private val getTemplateDetailUseCase: GetTemplateDetailUseCase
) {
    suspend operator fun invoke(workoutSession: WorkoutSession) {
        val templateId = workoutSession.templateId ?: return

        // Get the current template
        val currentTemplate = getTemplateDetailUseCase(templateId).first() ?: return

        // Convert session exercises to template exercises
        val updatedExercises = workoutSession.exercises.mapIndexed { index, sessionExercise ->
            // Try to find existing template exercise for this exercise
            val existingTemplateExercise = currentTemplate.exercises.find {
                it.exercise.id == sessionExercise.exercise.id
            }

            TemplateExercise(
                id = existingTemplateExercise?.id ?: UUID.randomUUID().toString(),
                exercise = sessionExercise.exercise,
                orderIndex = index,
                targetSets = sessionExercise.sets.size,
                targetReps = existingTemplateExercise?.targetReps ?: "6-10",
                restTimerSeconds = existingTemplateExercise?.restTimerSeconds ?: 90,
                note = existingTemplateExercise?.note ?: ""
            )
        }

        // Update the template with new exercises
        val updatedTemplate = currentTemplate.copy(
            exercises = updatedExercises
        )

        repository.saveTemplate(updatedTemplate)
    }
}
