package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import java.util.UUID

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val bodyPart: BodyPart,
    val category: ExerciseCategory,
    val measureType: MeasureType
)