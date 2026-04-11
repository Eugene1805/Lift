package com.eugene.lift.data.mapper

import com.eugene.lift.data.local.dao.TemplateExerciseDetail
import com.eugene.lift.data.local.dao.TemplateWithExercises
import com.eugene.lift.data.local.entity.WorkoutTemplateEntity
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.TemplateExercise
import com.eugene.lift.domain.model.WorkoutTemplate

fun TemplateWithExercises.toDomain(): WorkoutTemplate {
    return WorkoutTemplate(
        id = template.id,
        name = template.name,
        notes = template.notes,
        isArchived = template.isArchived,
        lastPerformedAt = template.lastPerformedAt,
        exercises = exercises
            .sortedBy { it.templateExercise.orderIndex }
            .map { it.toDomain() },
        folderId = template.folderId,
        sortOrder = template.sortOrder
    )
}

fun TemplateExerciseDetail.toDomain(): TemplateExercise {
    val domainExercise = Exercise(
        id = exercise.id,
        name = exercise.name,
        category = exercise.category,
        measureType = exercise.measureType,
        instructions = exercise.instructions,
        imagePath = exercise.imagePath,
        // Mapeo real: CrossRef -> Enum
        bodyParts = bodyPartRefs.map { it.bodyPart }
    )

    return TemplateExercise(
        id = templateExercise.id,
        exercise = domainExercise,
        orderIndex = templateExercise.orderIndex,
        targetSets = templateExercise.targetSets,
        targetReps = templateExercise.targetReps,
        restTimerSeconds = templateExercise.restTimerSeconds,
        note = templateExercise.note
    )
}

fun WorkoutTemplate.toEntity() = WorkoutTemplateEntity(
    id = id,
    name = name,
    notes = notes,
    isArchived = isArchived,
    lastPerformedAt = lastPerformedAt,
    folderId = folderId,
    sortOrder = sortOrder
)