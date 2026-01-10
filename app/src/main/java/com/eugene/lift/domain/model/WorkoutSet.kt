package com.eugene.lift.domain.model

data class WorkoutSet(
    val id: String,
    val weight: Double,
    val reps: Int,
    val completed: Boolean = true,
    val rpe: Double? = null,
    val rir: Int? = null,
    val isPr: Boolean = false
)