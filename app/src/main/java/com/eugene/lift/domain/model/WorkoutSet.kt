package com.eugene.lift.domain.model

/**
 * Represents a single set performed within an exercise.
 *
 * @property id Unique identifier for the set.
 * @property weight The amount of weight lifted.
 * @property reps The number of repetitions performed.
 * @property completed Whether the set was successfully completed.
 * @property rpe Rate of Perceived Exertion (0-10).
 * @property rir Reps in Reserve.
 * @property isPr Whether this set constitutes a Personal Record.
 * @property timeSeconds Duration of the set in seconds (for time-based exercises).
 * @property distance Distance covered (for distance-based exercises).
 */
data class WorkoutSet(
    val id: String,
    val weight: Double,
    val reps: Int,
    val completed: Boolean = false,
    val rpe: Double? = null,
    val rir: Int? = null,
    val isPr: Boolean = false,
    val timeSeconds: Long? = null,
    val distance: Double? = null
)