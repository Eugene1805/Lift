package com.eugene.lift.domain.model

/**
 * Represents the current state of a countdown timer.
 *
 * @property isRunning Whether the timer is currently active.
 * @property timeRemainingSeconds Remaining time in seconds.
 * @property totalTimeSeconds The initial duration for which the timer was set. Used for progress calculation.
 * @property progress Normalized progress value from 0.0 to 1.0.
 */
data class TimerState(
    val isRunning: Boolean = false,
    val timeRemainingSeconds: Long = 0,
    val totalTimeSeconds: Long = 0,
    val progress: Float = 0f
)