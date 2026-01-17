package com.eugene.lift.domain.model

data class WorkoutSet(
    val id: String,
    val weight: Double,
    val reps: Int,
    val completed: Boolean = false,
    val rpe: Double? = null,
    val rir: Int? = null,
    val isPr: Boolean = false,
    val timeSeconds: Long? = null, // Para TIME y DISTANCE_TIME
    val distance: Double? = null
)