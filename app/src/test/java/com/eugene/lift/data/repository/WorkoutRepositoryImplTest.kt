package com.eugene.lift.data.repository

import com.eugene.lift.data.local.dao.SessionComplete
import com.eugene.lift.data.local.dao.WorkoutDao
import com.eugene.lift.data.local.entity.WorkoutSessionEntity
import com.eugene.lift.data.local.entity.WorkoutSetEntity
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.repository.SettingsRepository
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.math.abs

class WorkoutRepositoryImplTest {

    private lateinit var dao: WorkoutDao
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var repository: WorkoutRepositoryImpl

    @Before
    fun setup() {
        dao = mockk()
        settingsRepository = mockk()
        
        every { settingsRepository.getSettings() } returns flowOf(UserSettings(weightUnit = WeightUnit.KG))
        
        repository = WorkoutRepositoryImpl(dao, settingsRepository)
    }

    @Test
    fun `getPersonalRecord returns domain set correctly converted`() = runTest {
        val exerciseId = "ex1"
        val setEntity = WorkoutSetEntity(
            id = "set1",
            sessionExerciseId = "se1",
            orderIndex = 0,
            weight = 100.0,
            reps = 10,
            completed = true,
            isPr = true
        )
        
        coEvery { dao.getPersonalRecordSet(exerciseId) } returns setEntity

        val result = repository.getPersonalRecord(exerciseId).first()

        assertEquals(100.0, result?.weight)
        assertEquals(10, result?.reps)
        assertEquals(true, result?.isPr)
    }

    @Test
    fun `getPersonalRecord with LBS preference converts weight to LBS`() = runTest {
        val exerciseId = "ex1"
        every { settingsRepository.getSettings() } returns flowOf(UserSettings(weightUnit = WeightUnit.LBS))
        
        val setEntity = WorkoutSetEntity(
            id = "set1",
            sessionExerciseId = "se1",
            orderIndex = 0,
            weight = 100.0, // Stored in KG
            reps = 10,
            completed = true,
            isPr = true
        )
        
        coEvery { dao.getPersonalRecordSet(exerciseId) } returns setEntity

        val result = repository.getPersonalRecord(exerciseId).first()

        // 100 kg * 2.20462 = 220.462 lbs -> rounded to 1 decimal = 220.5 lbs
        assertEquals(220.5, result!!.weight, 0.01)
    }

    @Test
    fun `getPersonalRecord returns null if no PR found`() = runTest {
        val exerciseId = "ex1"
        coEvery { dao.getPersonalRecordSet(exerciseId) } returns null

        val result = repository.getPersonalRecord(exerciseId).first()

        assertNull(result)
    }

    @Test
    fun `saveSession maps to entities and saves in KG regardless of preference`() = runTest {
        every { settingsRepository.getSettings() } returns flowOf(UserSettings(weightUnit = WeightUnit.LBS))
        
        val session = WorkoutSession(
            id = "session1",
            name = "Test Session",
            date = LocalDateTime.now(),
            templateId = null,
            durationSeconds = 0,
            exercises = listOf(
                SessionExercise(
                    id = "se1",
                    exercise = Exercise("ex1", "Squat", ExerciseCategory.BARBELL, MeasureType.REPS_AND_WEIGHT, "", null, emptyList()),
                    sets = listOf(
                        WorkoutSet(id = "set1", weight = 220.462, reps = 5, completed = true)
                    )
                )
            )
        )

        coEvery { dao.saveSessionComplete(any(), any(), any()) } returns Unit

        repository.saveSession(session)

        coVerify { 
            dao.saveSessionComplete(
                match { it.id == "session1" },
                match { it.first().id == "se1" },
                // Expect LBS to be converted BACK to KG for storage: 220.462 / 2.20462 ≈ 100.0
                match { abs(it.first().weight - 100.0) < 0.1 }
            )
        }
    }

    @Test
    fun `getLastHistoryForExercise returns complete session converted to preferences`() = runTest {
        val exerciseId = "ex1"
        every { settingsRepository.getSettings() } returns flowOf(UserSettings(weightUnit = WeightUnit.LBS))
        
        val sessionEntity = WorkoutSessionEntity("session1", null, "Test", LocalDateTime.now(), 3600, null)
        val complete = SessionComplete(
            session = sessionEntity,
            exercises = emptyList() // Simplified for testing
        )
        
        coEvery { dao.getLastSessionWithExercise(exerciseId, any()) } returns complete

        val result = repository.getLastHistoryForExercise(exerciseId)

        assertEquals("Test", result?.name)
    }

    @Test
    fun `getLastHistoryForExercise prefers last session from same template when templateId is provided`() = runTest {
        val exerciseId = "ex1"
        val templateId = "template-1"

        val sessionSameTemplate = SessionComplete(
            session = WorkoutSessionEntity("s1", templateId, "Same template", LocalDateTime.now().minusDays(2), 3600, null),
            exercises = emptyList()
        )
        val sessionAny = SessionComplete(
            session = WorkoutSessionEntity("s2", null, "Any template", LocalDateTime.now().minusDays(1), 3600, null),
            exercises = emptyList()
        )

        // When asking for same template, return that session; repository must not fall back.
        coEvery { dao.getLastSessionWithExercise(exerciseId, templateId, any()) } returns sessionSameTemplate
        // Fallback (any template) exists but should not be used.
        coEvery { dao.getLastSessionWithExercise(exerciseId, null, any()) } returns sessionAny

        val result = repository.getLastHistoryForExercise(exerciseId, templateId)

        assertEquals("Same template", result?.name)
    }

    @Test
    fun `getLastHistoryForExercise falls back to any session when no same-template session exists`() = runTest {
        val exerciseId = "ex1"
        val templateId = "template-1"

        val sessionAny = SessionComplete(
            session = WorkoutSessionEntity("s2", null, "Any template", LocalDateTime.now().minusDays(1), 3600, null),
            exercises = emptyList()
        )

        coEvery { dao.getLastSessionWithExercise(exerciseId, templateId, any()) } returns null
        coEvery { dao.getLastSessionWithExercise(exerciseId, null, any()) } returns sessionAny

        val result = repository.getLastHistoryForExercise(exerciseId, templateId)

        assertEquals("Any template", result?.name)
    }
}
