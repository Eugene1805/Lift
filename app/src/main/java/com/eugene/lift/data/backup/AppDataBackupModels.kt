package com.eugene.lift.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class AppDataBackupPayload(
    val schemaVersion: Int,
    val exportedAt: String,
    val settings: SettingsBackupSnapshot,
    val folders: List<FolderBackupDto>,
    val exercises: List<ExerciseBackupDto>,
    val templates: List<WorkoutTemplateBackupDto>,
    val templateExercises: List<TemplateExerciseBackupDto>,
    val sessions: List<WorkoutSessionBackupDto>,
    val sessionExercises: List<SessionExerciseBackupDto>,
    val workoutSets: List<WorkoutSetBackupDto>,
    val userProfiles: List<UserProfileBackupDto>
)

@Serializable
data class SettingsBackupSnapshot(
    val theme: String?,
    val weightUnit: String?,
    val distanceUnit: String?,
    val languageCode: String?,
    val trackedExerciseIds: List<String>,
    val onboardingComplete: Boolean,
    val swipeHintSeen: Boolean,
    val effortMetric: String?,
    val autoTimerEnabled: Boolean
)

@Serializable
data class FolderBackupDto(
    val id: String,
    val name: String,
    val color: String,
    val createdAt: Long
)

@Serializable
data class ExerciseBackupDto(
    val id: String,
    val name: String,
    val category: String,
    val measureType: String,
    val instructions: String,
    val imagePath: String?,
    val seedKey: String?,
    val bodyParts: List<String>
)

@Serializable
data class WorkoutTemplateBackupDto(
    val id: String,
    val name: String,
    val notes: String,
    val isArchived: Boolean,
    val lastPerformedAt: String?,
    val folderId: String?,
    val sortOrder: Int
)

@Serializable
data class TemplateExerciseBackupDto(
    val id: String,
    val templateId: String,
    val exerciseId: String,
    val orderIndex: Int,
    val targetSets: Int,
    val targetReps: String,
    val restTimerSeconds: Int,
    val note: String
)

@Serializable
data class WorkoutSessionBackupDto(
    val id: String,
    val templateId: String?,
    val name: String,
    val date: String,
    val durationSeconds: Long,
    val note: String?
)

@Serializable
data class SessionExerciseBackupDto(
    val id: String,
    val sessionId: String,
    val exerciseId: String,
    val orderIndex: Int,
    val note: String?
)

@Serializable
data class WorkoutSetBackupDto(
    val id: String,
    val sessionExerciseId: String,
    val orderIndex: Int,
    val weight: Double,
    val reps: Int,
    val completed: Boolean,
    val rpe: Double?,
    val rir: Int?,
    val isPr: Boolean,
    val timeSeconds: Long?,
    val distance: Double?
)

@Serializable
data class UserProfileBackupDto(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String?,
    val avatarUrl: String?,
    val avatarColor: String,
    val bio: String?,
    val createdAt: String,
    val updatedAt: String,
    val totalWorkouts: Int,
    val totalVolume: Double,
    val totalDuration: Long,
    val totalPRs: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastWorkoutDate: String?,
    val followersCount: Int,
    val followingCount: Int,
    val isPublic: Boolean
)
