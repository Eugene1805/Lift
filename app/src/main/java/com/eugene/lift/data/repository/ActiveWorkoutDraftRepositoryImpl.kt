package com.eugene.lift.data.repository

import com.eugene.lift.data.local.ActiveWorkoutDraftDataSource
import com.eugene.lift.domain.model.ActiveWorkoutDraft
import com.eugene.lift.domain.model.ActiveWorkoutDraftSummary
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.ActiveWorkoutDraftRepository
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Singleton
class ActiveWorkoutDraftRepositoryImpl @Inject constructor(
    private val dataSource: ActiveWorkoutDraftDataSource
) : ActiveWorkoutDraftRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun observeSummary(): Flow<ActiveWorkoutDraftSummary?> {
        return dataSource.draftJson.map { encoded ->
            encoded?.let(::decodeDraft)?.toSummary()
        }
    }

    override suspend fun getDraft(): ActiveWorkoutDraft? {
        return dataSource.draftJson.first()?.let(::decodeDraft)
    }

    override suspend fun saveDraft(draft: ActiveWorkoutDraft) {
        dataSource.writeDraftJson(json.encodeToString(ActiveWorkoutDraftDto.serializer(), draft.toDto()))
    }

    override suspend fun clearDraft() {
        dataSource.clearDraft()
    }

    private fun decodeDraft(encoded: String): ActiveWorkoutDraft? {
        return runCatching {
            json.decodeFromString(ActiveWorkoutDraftDto.serializer(), encoded).toDomain()
        }.getOrNull()
    }
}

@Serializable
private data class ActiveWorkoutDraftDto(
    val session: WorkoutSessionDto,
    val originalTemplateExercises: List<SessionExerciseDto>,
    val startedAtEpochMillis: Long,
    val lastInteractedAtEpochMillis: Long
)

@Serializable
private data class WorkoutSessionDto(
    val id: String,
    val templateId: String?,
    val name: String,
    val date: String,
    val durationSeconds: Long,
    val exercises: List<SessionExerciseDto>,
    val note: String?
)

@Serializable
private data class SessionExerciseDto(
    val id: String,
    val exercise: ExerciseDto,
    val sets: List<WorkoutSetDto>,
    val note: String?
)

@Serializable
private data class ExerciseDto(
    val id: String,
    val name: String,
    val category: String,
    val measureType: String,
    val instructions: String,
    val imagePath: String?,
    val bodyParts: List<String>,
    val seedKey: String?
)

@Serializable
private data class WorkoutSetDto(
    val id: String,
    val weight: Double,
    val reps: Int,
    val completed: Boolean,
    val rpe: Double?,
    val rir: Int?,
    val isPr: Boolean,
    val timeSeconds: Long?,
    val distance: Double?
)

private fun ActiveWorkoutDraft.toDto(): ActiveWorkoutDraftDto {
    return ActiveWorkoutDraftDto(
        session = session.toDto(),
        originalTemplateExercises = originalTemplateExercises.map(SessionExercise::toDto),
        startedAtEpochMillis = startedAtEpochMillis,
        lastInteractedAtEpochMillis = lastInteractedAtEpochMillis
    )
}

private fun ActiveWorkoutDraftDto.toDomain(): ActiveWorkoutDraft {
    return ActiveWorkoutDraft(
        session = session.toDomain(),
        originalTemplateExercises = originalTemplateExercises.map(SessionExerciseDto::toDomain),
        startedAtEpochMillis = startedAtEpochMillis,
        lastInteractedAtEpochMillis = lastInteractedAtEpochMillis
    )
}

private fun WorkoutSession.toDto(): WorkoutSessionDto {
    return WorkoutSessionDto(
        id = id,
        templateId = templateId,
        name = name,
        date = date.toString(),
        durationSeconds = durationSeconds,
        exercises = exercises.map(SessionExercise::toDto),
        note = note
    )
}

private fun WorkoutSessionDto.toDomain(): WorkoutSession {
    return WorkoutSession(
        id = id,
        templateId = templateId,
        name = name,
        date = LocalDateTime.parse(date),
        durationSeconds = durationSeconds,
        exercises = exercises.map(SessionExerciseDto::toDomain),
        note = note
    )
}

private fun SessionExercise.toDto(): SessionExerciseDto {
    return SessionExerciseDto(
        id = id,
        exercise = exercise.toDto(),
        sets = sets.map(WorkoutSet::toDto),
        note = note
    )
}

private fun SessionExerciseDto.toDomain(): SessionExercise {
    return SessionExercise(
        id = id,
        exercise = exercise.toDomain(),
        sets = sets.map(WorkoutSetDto::toDomain),
        note = note
    )
}

private fun Exercise.toDto(): ExerciseDto {
    return ExerciseDto(
        id = id,
        name = name,
        category = category.name,
        measureType = measureType.name,
        instructions = instructions,
        imagePath = imagePath,
        bodyParts = bodyParts.map(BodyPart::name),
        seedKey = seedKey
    )
}

private fun ExerciseDto.toDomain(): Exercise {
    return Exercise(
        id = id,
        name = name,
        category = ExerciseCategory.valueOf(category),
        measureType = MeasureType.valueOf(measureType),
        instructions = instructions,
        imagePath = imagePath,
        bodyParts = bodyParts.map(BodyPart::valueOf),
        seedKey = seedKey
    )
}

private fun WorkoutSet.toDto(): WorkoutSetDto {
    return WorkoutSetDto(
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

private fun WorkoutSetDto.toDomain(): WorkoutSet {
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
