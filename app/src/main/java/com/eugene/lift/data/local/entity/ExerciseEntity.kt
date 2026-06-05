package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.ExerciseSource
import com.eugene.lift.domain.model.MeasureType
import java.util.UUID

@Entity(
    tableName = "exercises",
    indices = [Index(value = ["remoteId"], unique = true)]
)
data class ExerciseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: ExerciseCategory,
    val measureType: MeasureType,
    val instructions: String = "",
    val imagePath: String? = null,
    val remoteId: Int? = null,
    val source: ExerciseSource = ExerciseSource.LOCAL,
    val lastSyncedAt: Long? = null,
    val syncVersion: Int? = null
)
