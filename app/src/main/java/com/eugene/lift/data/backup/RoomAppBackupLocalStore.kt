package com.eugene.lift.data.backup

import androidx.room.withTransaction
import com.eugene.lift.data.local.ActiveWorkoutDraftDataSource
import com.eugene.lift.data.local.AppDatabase
import com.eugene.lift.data.local.SettingsDataSource
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.dao.FolderDao
import com.eugene.lift.data.local.dao.TemplateDao
import com.eugene.lift.data.local.dao.UserProfileDao
import com.eugene.lift.data.local.dao.WorkoutDao
import com.eugene.lift.data.local.entity.ExerciseBodyPartCrossRef
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.data.local.entity.FolderEntity
import com.eugene.lift.data.local.entity.SessionExerciseEntity
import com.eugene.lift.data.local.entity.TemplateExerciseEntity
import com.eugene.lift.data.local.entity.UserProfileEntity
import com.eugene.lift.data.local.entity.WorkoutSessionEntity
import com.eugene.lift.data.local.entity.WorkoutSetEntity
import com.eugene.lift.data.local.entity.WorkoutTemplateEntity
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomAppBackupLocalStore @Inject constructor(
    private val database: AppDatabase,
    private val exerciseDao: ExerciseDao,
    private val templateDao: TemplateDao,
    private val workoutDao: WorkoutDao,
    private val folderDao: FolderDao,
    private val userProfileDao: UserProfileDao,
    private val settingsDataSource: SettingsDataSource,
    private val activeWorkoutDraftDataSource: ActiveWorkoutDraftDataSource
) : AppBackupLocalStore {

    override suspend fun exportPayload(): AppDataBackupPayload {
        val exercises = exerciseDao.getAllExerciseEntities()
        val bodyPartsByExercise = exerciseDao.getAllCrossRefs()
            .groupBy { it.exerciseId }

        return AppDataBackupPayload(
            schemaVersion = BACKUP_SCHEMA_VERSION,
            exportedAt = LocalDateTime.now().toString(),
            settings = settingsDataSource.getBackupSnapshot(),
            folders = folderDao.getAllFolderEntities().map { it.toBackupDto() },
            exercises = exercises.map { entity ->
                entity.toBackupDto(
                    bodyParts = bodyPartsByExercise[entity.id].orEmpty().map { it.bodyPart.name }
                )
            },
            templates = templateDao.getAllTemplateEntities().map { it.toBackupDto() },
            templateExercises = templateDao.getAllTemplateExerciseEntities().map { it.toBackupDto() },
            sessions = workoutDao.getAllSessionEntities().map { it.toBackupDto() },
            sessionExercises = workoutDao.getAllSessionExerciseEntities().map { it.toBackupDto() },
            workoutSets = workoutDao.getAllSetEntities().map { it.toBackupDto() },
            userProfiles = userProfileDao.getAllProfileEntities().map { it.toBackupDto() }
        )
    }

    override suspend fun importPayload(payload: AppDataBackupPayload) {
        database.withTransaction {
            clearLocalData()

            folderDao.insertFolders(payload.folders.map { it.toEntity() })
            exerciseDao.upsertExercises(payload.exercises.map { it.toEntity() })
            exerciseDao.insertCrossRefs(
                payload.exercises.flatMap { exercise ->
                    exercise.bodyParts.map { bodyPart ->
                        ExerciseBodyPartCrossRef(
                            exerciseId = exercise.id,
                            bodyPart = BodyPart.valueOf(bodyPart)
                        )
                    }
                }
            )
            templateDao.insertTemplates(payload.templates.map { it.toEntity() })
            templateDao.insertTemplateExercises(payload.templateExercises.map { it.toEntity() })
            workoutDao.insertSessions(payload.sessions.map { it.toEntity() })
            workoutDao.insertSessionExercises(payload.sessionExercises.map { it.toEntity() })
            workoutDao.insertSets(payload.workoutSets.map { it.toEntity() })
            userProfileDao.insertProfiles(payload.userProfiles.map { it.toEntity() })
        }

        settingsDataSource.restoreFromBackupSnapshot(payload.settings)
        activeWorkoutDraftDataSource.clearDraft()
    }

    companion object {
        const val BACKUP_SCHEMA_VERSION = 1
    }

    private suspend fun clearLocalData() {
        workoutDao.deleteAllSets()
        workoutDao.deleteAllSessionExercises()
        workoutDao.deleteAllSessions()
        templateDao.deleteAllTemplateExercises()
        templateDao.deleteAllTemplates()
        exerciseDao.deleteAllCrossRefs()
        exerciseDao.deleteAllExercises()
        folderDao.deleteAllFolders()
        userProfileDao.deleteAllProfiles()
    }
}

private fun FolderEntity.toBackupDto() = FolderBackupDto(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)

private fun ExerciseEntity.toBackupDto(bodyParts: List<String>) = ExerciseBackupDto(
    id = id,
    name = name,
    category = category.name,
    measureType = measureType.name,
    instructions = instructions,
    imagePath = imagePath,
    seedKey = seedKey,
    bodyParts = bodyParts
)

private fun WorkoutTemplateEntity.toBackupDto() = WorkoutTemplateBackupDto(
    id = id,
    name = name,
    notes = notes,
    isArchived = isArchived,
    lastPerformedAt = lastPerformedAt?.toString(),
    folderId = folderId,
    sortOrder = sortOrder
)

private fun TemplateExerciseEntity.toBackupDto() = TemplateExerciseBackupDto(
    id = id,
    templateId = templateId,
    exerciseId = exerciseId,
    orderIndex = orderIndex,
    targetSets = targetSets,
    targetReps = targetReps,
    restTimerSeconds = restTimerSeconds,
    note = note
)

private fun WorkoutSessionEntity.toBackupDto() = WorkoutSessionBackupDto(
    id = id,
    templateId = templateId,
    name = name,
    date = date.toString(),
    durationSeconds = durationSeconds,
    note = note
)

private fun SessionExerciseEntity.toBackupDto() = SessionExerciseBackupDto(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    orderIndex = orderIndex,
    note = note
)

private fun WorkoutSetEntity.toBackupDto() = WorkoutSetBackupDto(
    id = id,
    sessionExerciseId = sessionExerciseId,
    orderIndex = orderIndex,
    weight = weight,
    reps = reps,
    completed = completed,
    rpe = rpe,
    rir = rir,
    isPr = isPr,
    timeSeconds = timeSeconds,
    distance = distance
)

private fun UserProfileEntity.toBackupDto() = UserProfileBackupDto(
    id = id,
    username = username,
    displayName = displayName,
    email = email,
    avatarUrl = avatarUrl,
    avatarColor = avatarColor,
    bio = bio,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    totalWorkouts = totalWorkouts,
    totalVolume = totalVolume,
    totalDuration = totalDuration,
    totalPRs = totalPRs,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastWorkoutDate = lastWorkoutDate?.toString(),
    followersCount = followersCount,
    followingCount = followingCount,
    isPublic = isPublic
)

private fun FolderBackupDto.toEntity() = FolderEntity(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)

private fun ExerciseBackupDto.toEntity() = ExerciseEntity(
    id = id,
    name = name,
    category = ExerciseCategory.valueOf(category),
    measureType = MeasureType.valueOf(measureType),
    instructions = instructions,
    imagePath = imagePath,
    seedKey = seedKey
)

private fun WorkoutTemplateBackupDto.toEntity() = WorkoutTemplateEntity(
    id = id,
    name = name,
    notes = notes,
    isArchived = isArchived,
    lastPerformedAt = lastPerformedAt?.let(LocalDateTime::parse),
    folderId = folderId,
    sortOrder = sortOrder
)

private fun TemplateExerciseBackupDto.toEntity() = TemplateExerciseEntity(
    id = id,
    templateId = templateId,
    exerciseId = exerciseId,
    orderIndex = orderIndex,
    targetSets = targetSets,
    targetReps = targetReps,
    restTimerSeconds = restTimerSeconds,
    note = note
)

private fun WorkoutSessionBackupDto.toEntity() = WorkoutSessionEntity(
    id = id,
    templateId = templateId,
    name = name,
    date = LocalDateTime.parse(date),
    durationSeconds = durationSeconds,
    note = note
)

private fun SessionExerciseBackupDto.toEntity() = SessionExerciseEntity(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    orderIndex = orderIndex,
    note = note
)

private fun WorkoutSetBackupDto.toEntity() = WorkoutSetEntity(
    id = id,
    sessionExerciseId = sessionExerciseId,
    orderIndex = orderIndex,
    weight = weight,
    reps = reps,
    completed = completed,
    rpe = rpe,
    rir = rir,
    isPr = isPr,
    timeSeconds = timeSeconds,
    distance = distance
)

private fun UserProfileBackupDto.toEntity() = UserProfileEntity(
    id = id,
    username = username,
    displayName = displayName,
    email = email,
    avatarUrl = avatarUrl,
    avatarColor = avatarColor,
    bio = bio,
    createdAt = LocalDateTime.parse(createdAt),
    updatedAt = LocalDateTime.parse(updatedAt),
    totalWorkouts = totalWorkouts,
    totalVolume = totalVolume,
    totalDuration = totalDuration,
    totalPRs = totalPRs,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastWorkoutDate = lastWorkoutDate?.let(LocalDate::parse),
    followersCount = followersCount,
    followingCount = followingCount,
    isPublic = isPublic
)
