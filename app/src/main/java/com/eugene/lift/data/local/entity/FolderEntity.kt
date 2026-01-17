package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eugene.lift.domain.model.Folder

@Entity(tableName = "workout_folders")
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val createdAt: Long = System.currentTimeMillis()
)

fun FolderEntity.toDomain() = Folder(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)

// Extension function para mapear a entidad
fun Folder.toEntity() = FolderEntity(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)