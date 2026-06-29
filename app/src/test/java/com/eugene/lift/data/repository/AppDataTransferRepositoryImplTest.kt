package com.eugene.lift.data.repository

import com.eugene.lift.data.backup.AppBackupLocalStore
import com.eugene.lift.data.backup.AppDataBackupPayload
import com.eugene.lift.data.backup.ExerciseBackupDto
import com.eugene.lift.data.backup.FolderBackupDto
import com.eugene.lift.data.backup.RoomAppBackupLocalStore
import com.eugene.lift.data.backup.SessionExerciseBackupDto
import com.eugene.lift.data.backup.SettingsBackupSnapshot
import com.eugene.lift.data.backup.TemplateExerciseBackupDto
import com.eugene.lift.data.backup.UserProfileBackupDto
import com.eugene.lift.data.backup.WorkoutSessionBackupDto
import com.eugene.lift.data.backup.WorkoutSetBackupDto
import com.eugene.lift.data.backup.WorkoutTemplateBackupDto
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppDataTransferRepositoryImplTest {

    private lateinit var localStore: AppBackupLocalStore
    private lateinit var repository: AppDataTransferRepositoryImpl

    @Before
    fun setup() {
        localStore = mockk(relaxed = true)
        repository = AppDataTransferRepositoryImpl(localStore)
    }

    @Test
    fun `exportToJson serializes payload from local store`() = kotlinx.coroutines.test.runTest {
        coEvery { localStore.exportPayload() } returns samplePayload()

        val json = repository.exportToJson()

        assertTrue(json.contains("\"schemaVersion\": 1"))
        assertTrue(json.contains("\"Bench Press\""))
        assertTrue(json.contains("\"trackedExerciseIds\""))
        coVerify(exactly = 1) { localStore.exportPayload() }
    }

    @Test
    fun `importFromJson decodes payload and forwards to local store`() = kotlinx.coroutines.test.runTest {
        val exportRepository = AppDataTransferRepositoryImpl(object : AppBackupLocalStore {
            override suspend fun exportPayload(): AppDataBackupPayload = samplePayload()
            override suspend fun importPayload(payload: AppDataBackupPayload) = Unit
        })
        val json = exportRepository.exportToJson()

        val result = repository.importFromJson(json)

        assertTrue(result is AppResult.Success)
        coVerify(exactly = 1) { localStore.importPayload(withArg { payload ->
            assertEquals(RoomAppBackupLocalStore.BACKUP_SCHEMA_VERSION, payload.schemaVersion)
            assertEquals(1, payload.exercises.size)
            assertEquals("Bench Press", payload.exercises.first().name)
        }) }
    }

    @Test
    fun `importFromJson returns validation for unsupported schema`() = kotlinx.coroutines.test.runTest {
        val invalidJson = """
            {
              "schemaVersion": 999,
              "exportedAt": "2026-06-29T12:00:00",
              "settings": {
                "theme": "ORCA",
                "weightUnit": "KG",
                "distanceUnit": "KM",
                "languageCode": "es",
                "trackedExerciseIds": [],
                "onboardingComplete": true,
                "swipeHintSeen": true,
                "effortMetric": null,
                "autoTimerEnabled": true
              },
              "folders": [],
              "exercises": [],
              "templates": [],
              "templateExercises": [],
              "sessions": [],
              "sessionExercises": [],
              "workoutSets": [],
              "userProfiles": []
            }
        """.trimIndent()

        val result = repository.importFromJson(invalidJson)

        assertTrue(result is AppResult.Error)
        assertEquals(AppError.Validation, (result as AppResult.Error).error)
        coVerify(exactly = 0) { localStore.importPayload(any()) }
    }

    private fun samplePayload() = AppDataBackupPayload(
        schemaVersion = RoomAppBackupLocalStore.BACKUP_SCHEMA_VERSION,
        exportedAt = "2026-06-29T12:00:00",
        settings = SettingsBackupSnapshot(
            theme = "ORCA",
            weightUnit = "KG",
            distanceUnit = "KM",
            languageCode = "es",
            trackedExerciseIds = listOf("exercise-1"),
            onboardingComplete = true,
            swipeHintSeen = true,
            effortMetric = "RPE",
            autoTimerEnabled = true
        ),
        folders = listOf(FolderBackupDto("folder-1", "Push", "#FF0000", 123L)),
        exercises = listOf(
            ExerciseBackupDto(
                id = "exercise-1",
                name = "Bench Press",
                category = "BARBELL",
                measureType = "REPS_AND_WEIGHT",
                instructions = "Desc",
                imagePath = "bench_press",
                seedKey = "bench_press",
                bodyParts = listOf("CHEST")
            )
        ),
        templates = listOf(WorkoutTemplateBackupDto("template-1", "Push Day", "", false, null, "folder-1", 0)),
        templateExercises = listOf(TemplateExerciseBackupDto("template-ex-1", "template-1", "exercise-1", 0, 3, "8-10", 90, "")),
        sessions = listOf(WorkoutSessionBackupDto("session-1", "template-1", "Push Day", "2026-06-29T12:00:00", 1200, null)),
        sessionExercises = listOf(SessionExerciseBackupDto("session-ex-1", "session-1", "exercise-1", 0, null)),
        workoutSets = listOf(WorkoutSetBackupDto("set-1", "session-ex-1", 0, 100.0, 8, true, null, null, true, null, null)),
        userProfiles = listOf(
            UserProfileBackupDto(
                id = "profile-1",
                username = "eugene",
                displayName = "Eugene",
                email = null,
                avatarUrl = null,
                avatarColor = "#6200EE",
                bio = null,
                createdAt = "2026-06-29T12:00:00",
                updatedAt = "2026-06-29T12:00:00",
                totalWorkouts = 1,
                totalVolume = 1000.0,
                totalDuration = 1200,
                totalPRs = 1,
                currentStreak = 1,
                longestStreak = 1,
                lastWorkoutDate = "2026-06-29",
                followersCount = 0,
                followingCount = 0,
                isPublic = false
            )
        )
    )
}
