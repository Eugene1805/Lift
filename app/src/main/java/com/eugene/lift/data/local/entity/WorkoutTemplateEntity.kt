package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "workout_templates",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("folderId")]
)
data class WorkoutTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val notes: String,
    val isArchived: Boolean,
    val lastPerformedAt: LocalDateTime?,
    val folderId: String? = null
)
