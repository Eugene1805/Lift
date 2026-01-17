package com.eugene.lift.domain.model

import java.time.LocalDateTime

data class WorkoutTemplate(
    val id: String,
    val name: String,
    val notes: String = "",
    val exercises: List<TemplateExercise> = emptyList(),
    val isArchived: Boolean = false,
    val lastPerformedAt: LocalDateTime? = null,
    val folderId: String? = null
)