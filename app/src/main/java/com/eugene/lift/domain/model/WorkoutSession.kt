package com.eugene.lift.domain.model

import java.time.LocalDateTime

/**
 * Represents a completed or active workout session.
 *
 * @property id Unique identifier for the session.
 * @property templateId Identifier of the template used, if any.
 * @property name Name of the workout session.
 * @property date Timestamp of when the session occurred.
 * @property durationSeconds Total time elapsed during the session in seconds.
 * @property exercises List of exercises performed in this session.
 * @property note General notes about the session.
 */
data class WorkoutSession(
    val id: String,
    val templateId: String?,
    val name: String,
    val date: LocalDateTime,
    val durationSeconds: Long,
    val exercises: List<SessionExercise> = emptyList(),
    val note: String? = null
)
