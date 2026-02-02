package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.model.TemplateExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.repository.TemplateRepository
import java.util.UUID
import javax.inject.Inject

class CreateTemplateFromWorkoutUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(workoutSession: WorkoutSession) {
        // Convert session exercises to template exercises
        val templateExercises = workoutSession.exercises.mapIndexed { index, sessionExercise ->
            TemplateExercise(
                id = UUID.randomUUID().toString(),
                exercise = sessionExercise.exercise,
                orderIndex = index,
                targetSets = sessionExercise.sets.size,
                targetReps = "6-10", // Default target reps
                restTimerSeconds = 90, // Default rest timer
                note = ""
            )
        }

        // Create a new template
        val newTemplate = WorkoutTemplate(
            id = UUID.randomUUID().toString(),
            name = workoutSession.name,
            notes = "",
            exercises = templateExercises,
            isArchived = false,
            lastPerformedAt = null,
            folderId = null
        )

        repository.saveTemplate(newTemplate)
    }
}
