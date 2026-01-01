package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import com.eugene.lift.domain.model.BodyPart

@Entity(
    tableName = "exercise_body_part_cross_ref",
    primaryKeys = ["exerciseId", "bodyPart"],
    indices = [Index(value = ["bodyPart"])]
)
data class ExerciseBodyPartCrossRef(
    val exerciseId: String,
    val bodyPart: BodyPart
)