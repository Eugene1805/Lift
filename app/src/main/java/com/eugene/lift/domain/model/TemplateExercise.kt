package com.eugene.lift.domain.model

/**
 * An exercise definition within a workout template.
 *
 * @property id Unique identifier for this template-exercise link.
 * @property exercise The definition of the exercise to be performed.
 * @property orderIndex Sort order within the template.
 * @property targetSets Planned number of sets.
 * @property targetReps Planned repetitions or range (e.g., "8-12").
 * @property restTimerSeconds Default rest time between sets in seconds.
 * @property note Default notes for this exercise in the template.
 */
data class TemplateExercise(
    val id: String,
    val exercise: Exercise,
    val orderIndex: Int,
    val targetSets: Int = 3,
    val targetReps: String = "6-10",
    val restTimerSeconds: Int = 90,
    val note: String = ""
)