package com.eugene.lift.domain.model

import java.time.LocalDate

/**
 * A single data point on the exercise progression line chart.
 * Each point represents the *best set* (highest 1RM-equivalent) performed
 * for a given exercise during one session.
 *
 * We use the Epley formula for estimated 1RM: weight × (1 + reps / 30)
 * For reps-only exercises we store the max reps as the "value".
 */
data class ProgressionDataPoint(
    val date: LocalDate,
    /** Best set weight in the user's preferred unit (already converted by the repository). */
    val weight: Double,
    val reps: Int,
    /** e1RM = weight × (1 + reps / 30). 0 for reps-only exercises. */
    val estimatedOneRepMax: Double,
    val sessionName: String
)

/**
 * A personal record entry to display in the PR history list.
 */
data class PrRecord(
    val date: LocalDate,
    val weight: Double,
    val reps: Int,
    val sessionName: String
)

/**
 * Aggregated progression data for one tracked exercise.
 */
data class ExerciseProgression(
    val exerciseId: String,
    val exerciseName: String,
    /** Chronological list of data points for the line chart. */
    val dataPoints: List<ProgressionDataPoint>,
    /** All-time PR records, sorted newest first. */
    val prHistory: List<PrRecord>,
    /** The current (all-time best) personal record. */
    val currentPr: PrRecord?
)
