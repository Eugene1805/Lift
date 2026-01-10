package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("templateId")]
)
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val templateId: String?, // Null si fue "Quick Workout" o template borrado
    val name: String,
    val date: LocalDateTime,
    val durationSeconds: Long
)
