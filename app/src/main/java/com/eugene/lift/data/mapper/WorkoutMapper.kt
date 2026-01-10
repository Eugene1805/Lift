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
            .map { it.toDomain() }
    )
}

fun SessionExerciseWithSets.toDomain(): SessionExercise {
    // Nota: Aquí también asumimos que tu ExerciseEntity tiene lo básico.
    // Si necesitas bodyParts en el historial, deberías usar la misma lógica
    // que en TemplateMapper (trayendo CrossRefs en el DAO).
    // Por brevedad, usamos el mapeo simple aquí, pero idealmente sería igual al Template.
    return SessionExercise(
        id = sessionExercise.id,
        exercise = Exercise(
            id = exercise.id,
            name = exercise.name,
            category = exercise.category,
            measureType = exercise.measureType,
            instructions = exercise.instructions,
            imagePath = exercise.imagePath,
            bodyParts = emptyList() // TODO: Ajustar DAO si requieres esto en historial visual
        ),
        sets = sets
            .sortedBy { it.orderIndex }
            .map { it.toDomain() }
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
        isPr = isPr
    )
}

// --- Domain to Entity (Para Guardar) ---

fun WorkoutSession.toEntity() = WorkoutSessionEntity(
    id = id,
    templateId = templateId,
    name = name,
    date = date,
    durationSeconds = durationSeconds
)