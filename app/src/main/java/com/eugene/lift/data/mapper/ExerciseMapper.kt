package com.eugene.lift.data.mapper

import com.eugene.lift.data.local.dao.ExerciseResult
import com.eugene.lift.data.local.entity.ExerciseBodyPartCrossRef
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise

fun ExerciseResult.toDomain(): Exercise {
    val partList = if (bodyParts.isBlank()) {
        emptyList()
    } else {
        bodyParts.split(",").mapNotNull { raw ->
            try { BodyPart.valueOf(raw) } catch (e: Exception) { null }
        }
    }

    return Exercise(
        id = exercise.id,
        name = exercise.name,
        category = exercise.category,
        measureType = exercise.measureType,
        instructions = exercise.instructions,
        imagePath = exercise.imagePath,
        bodyParts = partList
    )
}

fun Exercise.toEntity(): ExerciseEntity {
    return ExerciseEntity(
        id = id,
        name = name,
        category = category,
        measureType = measureType,
        instructions = instructions,
        imagePath = imagePath
    )
}

fun Exercise.toCrossRefs(): List<ExerciseBodyPartCrossRef> {
    return bodyParts.map { part ->
        ExerciseBodyPartCrossRef(exerciseId = id, bodyPart = part)
    }
}