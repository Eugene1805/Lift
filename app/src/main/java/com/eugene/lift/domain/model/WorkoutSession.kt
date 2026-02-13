package com.eugene.lift.domain.model

import java.time.LocalDateTime

data class WorkoutSession(
    val id: String,
    val templateId: String?,
    val name: String,
    val date: LocalDateTime,
    val durationSeconds: Long,
    val exercises: List<SessionExercise> = emptyList(),
    // Optional session-level notes
    val note: String? = null
)
