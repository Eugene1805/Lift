package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "workout_templates")
data class WorkoutTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val notes: String,
    val isArchived: Boolean,
    val lastPerformedAt: LocalDateTime?
)
