package com.eugene.lift.domain.usecase.exercise

/**
 * Domain contract to resolve an exercise name into a drawable resource key.
 */
interface ExerciseImageResolver {
    fun resolveDrawable(exerciseName: String): String?
}

