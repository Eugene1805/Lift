package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import com.eugene.lift.domain.model.BodyPart

@Entity(
    tableName = "exercise_body_part_cross_ref",
    primaryKeys = ["exerciseId", "bodyPart"], // La PK compuesta evita duplicados
    indices = [Index(value = ["bodyPart"])]   // Índice para búsquedas rápidas por músculo
)
data class ExerciseBodyPartCrossRef(
    val exerciseId: String,
    val bodyPart: BodyPart // Room usará el Converter a String que ya hicimos
)