package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = SessionExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionExerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionExerciseId: String,
    val orderIndex: Int,
    // Peso almacenado SIEMPRE en kilogramos (KG) como unidad canónica
    val weight: Double,
    val reps: Int,
    val completed: Boolean,
    val rpe: Double? = null,
    val rir: Int? = null,
    val isPr: Boolean = false,
    val timeSeconds: Long? = null,
    val distance: Double? = null
)
