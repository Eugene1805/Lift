package com.eugene.lift.domain.model

import java.time.LocalDateTime

/**
 * A reusable template for creating workout sessions.
 *
 * @property id Unique identifier for the template.
 * @property name Name of the template.
 * @property notes Default notes or description for the template.
 * @property exercises List of planned exercises in the template.
 * @property isArchived Whether the template is hidden from the main list.
 * @property lastPerformedAt Timestamp of the most recent session using this template.
 * @property folderId Identifier of the folder containing this template.
 * @property sortOrder Position of the template in a sorted list.
 */
data class WorkoutTemplate(
    val id: String,
    val name: String,
    val notes: String = "",
    val exercises: List<TemplateExercise> = emptyList(),
    val isArchived: Boolean = false,
    val lastPerformedAt: LocalDateTime? = null,
    val folderId: String? = null,
    val sortOrder: Int = 0
)