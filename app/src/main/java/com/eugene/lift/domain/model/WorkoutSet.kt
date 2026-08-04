package com.eugene.lift.domain.model

import kotlin.math.abs
import kotlin.math.round

/**
 * Represents a single set performed within an exercise.
 *
 * @property id Unique identifier for the set.
 * @property weight The amount of weight lifted.
 * @property reps The number of repetitions performed.
 * @property completed Whether the set was successfully completed.
 * @property rpe Rate of Perceived Exertion (1-10, in 0.5 increments).
 * @property rir Reps in Reserve (0-10).
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
) {
    companion object {
        const val RPE_MIN = 1.0
        const val RPE_MAX = 10.0
        const val RPE_STEP = 0.5
        const val RIR_MIN = 0
        const val RIR_MAX = 10

        fun isValidRpe(value: Double): Boolean {
            val stepPosition = (value - RPE_MIN) / RPE_STEP
            return value in RPE_MIN..RPE_MAX &&
                abs(stepPosition - round(stepPosition)) < 0.000_001
        }

        fun isValidRir(value: Int): Boolean = value in RIR_MIN..RIR_MAX
    }
}
