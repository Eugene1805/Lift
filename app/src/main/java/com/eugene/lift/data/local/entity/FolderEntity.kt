package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_folders")
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val createdAt: Long = System.currentTimeMillis()
)