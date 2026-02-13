package com.eugene.lift.data.mapper

import com.eugene.lift.data.local.dao.SessionComplete
import com.eugene.lift.data.local.dao.SessionExerciseWithSets
import com.eugene.lift.data.local.entity.WorkoutSessionEntity
import com.eugene.lift.data.local.entity.WorkoutSetEntity
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet

fun SessionComplete.toDomain(): WorkoutSession {
    return WorkoutSession(
        id = session.id,
        templateId = session.templateId,
        name = session.name,
        date = session.date,
        durationSeconds = session.durationSeconds,
        exercises = exercises
            .sortedBy { it.sessionExercise.orderIndex }
            .map { it.toDomain() },
        note = session.note
    )
}

fun SessionExerciseWithSets.toDomain(): SessionExercise {
    return SessionExercise(
        id = sessionExercise.id,
        exercise = Exercise(
            id = exercise.id,
            name = exercise.name,
            category = exercise.category,
            measureType = exercise.measureType,
            instructions = exercise.instructions,
            imagePath = exercise.imagePath,
            bodyParts = bodyPartRefs.map { it.bodyPart }
        ),
        sets = sets
            .sortedBy { it.orderIndex }
            .map { it.toDomain() },
        note = sessionExercise.note
    )
}

fun WorkoutSetEntity.toDomain(): WorkoutSet {
    return WorkoutSet(
        id = id,
        weight = weight,
        reps = reps,
        completed = completed,
        rpe = rpe,
        rir = rir,
        isPr = isPr,
        timeSeconds = timeSeconds,
        distance = distance
    )
}

fun WorkoutSession.toEntity() = WorkoutSessionEntity(
    id = id,
    templateId = templateId,
    name = name,
    date = date,
    durationSeconds = durationSeconds,
    note = note
)