package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.UserSettings
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.SettingsRepository
import com.eugene.lift.domain.repository.UserProfileRepository
import com.eugene.lift.domain.repository.WorkoutRepository
import com.eugene.lift.domain.util.ExercisePerformanceEvaluator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit test for FinishWorkoutUseCase
 * Tests workout finishing logic including PR detection and validation
 */
class FinishWorkoutUseCaseTest {

    private lateinit var repository: WorkoutRepository
    private lateinit var useCase: FinishWorkoutUseCase

    private val sampleExercise = Exercise(
        id = "exercise-1",
        name = "Bench Press",
        bodyParts = listOf(BodyPart.CHEST),
        category = ExerciseCategory.BARBELL,
        measureType = MeasureType.REPS_AND_WEIGHT,
        instructions = "",
        imagePath = null
    )

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        val userProfileRepository = mockk<UserProfileRepository>(relaxed = true)
        val settingsRepository = mockk<SettingsRepository>(relaxed = true)
        coEvery { userProfileRepository.getCurrentProfileOnce() } returns null
        coEvery { settingsRepository.getSettings() } returns flowOf(UserSettings())
        every { repository.getExerciseHistory(any()) } returns flowOf(emptyList())
        useCase = FinishWorkoutUseCase(
            repository,
            userProfileRepository,
            settingsRepository,
            ExercisePerformanceEvaluator(),
            SafeExecutor(logger = null)
        )
    }

    @Test
    fun `invoke saves session with calculated duration`() = runTest {
        // GIVEN
        val startTime = LocalDateTime.now().minusMinutes(30)
        val session = createSampleSession(startTime)
        coEvery { repository.getPersonalRecord(any()) } returns flowOf(null)

        // WHEN
        val result = useCase(session)

        // THEN
        assertTrue(result is AppResult.Success)
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        // Duration should be approximately 30 minutes (1800 seconds), allow some margin
        assertTrue(savedSession.durationSeconds >= 1795)
        assertTrue(savedSession.durationSeconds <= 1805)
    }

    @Test
    fun `invoke marks PR when set beats previous record`() = runTest {
        // GIVEN
        val session = createSessionWithSet(weight = 100.0, completed = true)
        every {
            repository.getExerciseHistory("exercise-1")
        } returns flowOf(
            listOf(
                createHistorySession(
                    exercise = sampleExercise,
                    sets = listOf(createSet(weight = 90.0, completed = true))
                )
            )
        )

        // WHEN
        val result = useCase(session)

        // THEN
        assertTrue(result is AppResult.Success)
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        val savedSet = savedSession.exercises.first().sets.first()
        assertTrue("Set should be marked as PR", savedSet.isPr)
    }

    @Test
    fun `invoke does not mark PR when set does not beat previous record`() = runTest {
        // GIVEN
        val session = createSessionWithSet(weight = 80.0, completed = true)
        every {
            repository.getExerciseHistory("exercise-1")
        } returns flowOf(
            listOf(
                createHistorySession(
                    exercise = sampleExercise,
                    sets = listOf(createSet(weight = 100.0, completed = true))
                )
            )
        )

        // WHEN
        useCase(session)

        // THEN
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        val savedSet = savedSession.exercises.first().sets.first()
        assertFalse("Set should NOT be marked as PR", savedSet.isPr)
    }

    @Test
    fun `invoke marks PR when set equals previous record`() = runTest {
        // GIVEN - Equal weight, not greater
        val session = createSessionWithSet(weight = 100.0, completed = true)
        every {
            repository.getExerciseHistory("exercise-1")
        } returns flowOf(
            listOf(
                createHistorySession(
                    exercise = sampleExercise,
                    sets = listOf(createSet(weight = 100.0, completed = true))
                )
            )
        )

        // WHEN
        useCase(session)

        // THEN
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        val savedSet = savedSession.exercises.first().sets.first()
        // Equal weight is NOT a PR (only > counts)
        assertFalse("Set with equal weight should NOT be marked as PR", savedSet.isPr)
    }

    @Test
    fun `invoke marks PR for first ever set of exercise`() = runTest {
        // GIVEN - No previous record
        val session = createSessionWithSet(weight = 50.0, completed = true)

        // WHEN
        useCase(session)

        // THEN
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        val savedSet = savedSession.exercises.first().sets.first()
        assertTrue("First set ever should be marked as PR", savedSet.isPr)
    }

    @Test
    fun `invoke only marks PR on heaviest set when multiple sets completed`() = runTest {
        // GIVEN
        val sets = listOf(
            createSet(weight = 80.0, completed = true),
            createSet(weight = 100.0, completed = true), // This is the PR
            createSet(weight = 90.0, completed = true)
        )
        val session = createSessionWithSets(sets)
        every {
            repository.getExerciseHistory("exercise-1")
        } returns flowOf(
            listOf(
                createHistorySession(
                    exercise = sampleExercise,
                    sets = listOf(createSet(weight = 95.0, completed = true))
                )
            )
        )

        // WHEN
        useCase(session)

        // THEN
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        val savedSets = savedSession.exercises.first().sets

        assertFalse("80kg set should NOT be PR", savedSets[0].isPr)
        assertTrue("100kg set should be PR", savedSets[1].isPr)
        assertFalse("90kg set should NOT be PR", savedSets[2].isPr)
    }

    @Test
    fun `invoke marks all sets with PR weight when multiple sets achieve same max weight`() = runTest {
        // GIVEN - Two sets with same max weight
        val sets = listOf(
            createSet(weight = 100.0, completed = true), // PR
            createSet(weight = 90.0, completed = true),
            createSet(weight = 100.0, completed = true)  // Also PR
        )
        val session = createSessionWithSets(sets)
        every {
            repository.getExerciseHistory("exercise-1")
        } returns flowOf(
            listOf(
                createHistorySession(
                    exercise = sampleExercise,
                    sets = listOf(createSet(weight = 95.0, completed = true))
                )
            )
        )

        // WHEN
        useCase(session)

        // THEN
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        val savedSets = savedSession.exercises.first().sets

        assertTrue("First 100kg set should be PR", savedSets[0].isPr)
        assertFalse("90kg set should NOT be PR", savedSets[1].isPr)
        assertTrue("Second 100kg set should also be PR", savedSets[2].isPr)
    }

    @Test
    fun `invoke does not mark incomplete sets as PR`() = runTest {
        // GIVEN
        val sets = listOf(
            createSet(weight = 100.0, completed = false), // Not completed
            createSet(weight = 80.0, completed = true)
        )
        val session = createSessionWithSets(sets)

        // WHEN
        useCase(session)

        // THEN
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        val savedSets = savedSession.exercises.first().sets

        assertEquals(1, savedSets.size)
        assertTrue("Completed set should be PR", savedSets[0].isPr)
    }

    @Test
    fun `invoke removes exercises with no sets`() = runTest {
        // GIVEN
        val exerciseWithNoSets = SessionExercise(
            id = "session-ex-2",
            exercise = sampleExercise.copy(id = "exercise-2", name = "Empty Exercise"),
            sets = emptyList()
        )
        val session = createSampleSession(LocalDateTime.now()).copy(
            exercises = listOf(
                createSessionExercise(listOf(createSet(weight = 80.0, completed = true))),
                exerciseWithNoSets
            )
        )
        coEvery { repository.getPersonalRecord(any()) } returns flowOf(null)

        // WHEN
        useCase(session)

        // THEN
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        assertEquals("Empty exercise should be filtered out", 1, savedSession.exercises.size)
    }

    @Test
    fun `invoke returns Validation error when workout has no completed exercises`() = runTest {
        // GIVEN - All exercises have no sets
        val emptyExercise = SessionExercise(
            id = "session-ex-1",
            exercise = sampleExercise,
            sets = emptyList()
        )
        val session = createSampleSession(LocalDateTime.now()).copy(
            exercises = listOf(emptyExercise)
        )

        // WHEN
        val result = useCase(session)

        // THEN
        assertTrue(result is AppResult.Error)
        assertEquals(AppError.Validation, (result as AppResult.Error).error)
        coVerify(exactly = 0) { repository.saveSession(any()) }
    }

    @Test
    fun `invoke returns Validation error when workout has only incomplete sets`() = runTest {
        // GIVEN
        val sets = listOf(
            createSet(weight = 100.0, completed = false),
            createSet(weight = 90.0, completed = false)
        )
        val session = createSessionWithSets(sets)

        // WHEN
        val result = useCase(session)

        // THEN
        assertTrue(result is AppResult.Error)
        assertEquals(AppError.Validation, (result as AppResult.Error).error)
        coVerify(exactly = 0) { repository.saveSession(any()) }
    }

    @Test
    fun `invoke drops incomplete sets when workout has completed work`() = runTest {
        // GIVEN
        val sets = listOf(
            createSet(weight = 100.0, completed = true),
            createSet(weight = 90.0, completed = false)
        )
        val session = createSessionWithSets(sets)
        every { repository.getExerciseHistory("exercise-1") } returns flowOf(emptyList())

        // WHEN
        val result = useCase(session)

        // THEN
        assertTrue(result is AppResult.Success)
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        val savedSets = savedSession.exercises.first().sets
        assertEquals(1, savedSets.size)
        assertTrue(savedSets.all { it.completed })
    }

    @Test
    fun `invoke handles multiple exercises with different PRs`() = runTest {
        // GIVEN
        val exercise2 = sampleExercise.copy(id = "exercise-2", name = "Squat")
        val session = createSampleSession(LocalDateTime.now()).copy(
            exercises = listOf(
                createSessionExercise(
                    listOf(createSet(weight = 100.0, completed = true)),
                    sampleExercise
                ),
                createSessionExercise(
                    listOf(createSet(weight = 150.0, completed = true)),
                    exercise2
                )
            )
        )

        every { repository.getExerciseHistory("exercise-1") } returns flowOf(
            listOf(
                createHistorySession(
                    exercise = sampleExercise,
                    sets = listOf(createSet(weight = 95.0, completed = true))
                )
            )
        )
        every { repository.getExerciseHistory("exercise-2") } returns flowOf(
            listOf(
                createHistorySession(
                    exercise = exercise2,
                    sets = listOf(createSet(weight = 140.0, completed = true))
                )
            )
        )

        // WHEN
        useCase(session)

        // THEN
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        assertTrue("First exercise should have PR", savedSession.exercises[0].sets[0].isPr)
        assertTrue("Second exercise should have PR", savedSession.exercises[1].sets[0].isPr)
    }

    @Test
    fun `invoke marks PR when estimated one rep max improves at same weight`() = runTest {
        val session = createSessionWithSet(weight = 100.0, completed = true, reps = 8)
        val previousSession = createHistorySession(
            exercise = sampleExercise,
            sets = listOf(createSet(weight = 100.0, completed = true, reps = 5))
        )
        coEvery { repository.getExerciseHistory("exercise-1") } returns flowOf(listOf(previousSession))

        useCase(session)

        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSet = slot.captured.exercises.first().sets.first()
        assertTrue("Set should be PR when e1RM improves", savedSet.isPr)
    }

    @Test
    fun `invoke marks PR for reps only exercises using reps instead of weight`() = runTest {
        val repsOnlyExercise = sampleExercise.copy(
            id = "exercise-reps",
            name = "Pull Up",
            category = ExerciseCategory.BODYWEIGHT,
            measureType = MeasureType.REPS_ONLY
        )
        val session = createSampleSession(LocalDateTime.now()).copy(
            exercises = listOf(
                createSessionExercise(
                    sets = listOf(createSet(weight = 0.0, completed = true, reps = 12)),
                    exercise = repsOnlyExercise
                )
            )
        )
        val previousSession = createHistorySession(
            exercise = repsOnlyExercise,
            sets = listOf(createSet(weight = 0.0, completed = true, reps = 10))
        )
        coEvery { repository.getExerciseHistory("exercise-reps") } returns flowOf(listOf(previousSession))

        useCase(session)

        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSet = slot.captured.exercises.first().sets.first()
        assertTrue("Higher reps should be marked as PR for reps-only exercises", savedSet.isPr)
    }

    @Test
    fun `invoke does not mark PR when same weight has lower estimated one rep max`() = runTest {
        val session = createSessionWithSet(weight = 100.0, completed = true, reps = 3)
        val previousSession = createHistorySession(
            exercise = sampleExercise,
            sets = listOf(createSet(weight = 100.0, completed = true, reps = 6))
        )
        coEvery { repository.getExerciseHistory("exercise-1") } returns flowOf(listOf(previousSession))

        useCase(session)

        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSet = slot.captured.exercises.first().sets.first()
        assertFalse("Lower e1RM should not be marked as PR", savedSet.isPr)
    }

    // Helper methods
    private fun createSampleSession(startTime: LocalDateTime): WorkoutSession {
        return WorkoutSession(
            id = "session-1",
            templateId = "template-1",
            name = "Test Workout",
            date = startTime,
            durationSeconds = 0,
            exercises = listOf(
                createSessionExercise(listOf(createSet(weight = 80.0, completed = true)))
            )
        )
    }

    private fun createSessionWithSet(weight: Double, completed: Boolean, reps: Int = 10): WorkoutSession {
        return createSampleSession(LocalDateTime.now()).copy(
            exercises = listOf(
                createSessionExercise(listOf(createSet(weight, completed, reps)))
            )
        )
    }

    private fun createSessionWithSets(sets: List<WorkoutSet>): WorkoutSession {
        return createSampleSession(LocalDateTime.now()).copy(
            exercises = listOf(createSessionExercise(sets))
        )
    }

    private fun createSessionExercise(
        sets: List<WorkoutSet>,
        exercise: Exercise = sampleExercise
    ): SessionExercise {
        return SessionExercise(
            id = "session-ex-1",
            exercise = exercise,
            sets = sets
        )
    }

    private fun createHistorySession(
        exercise: Exercise,
        sets: List<WorkoutSet>
    ): WorkoutSession {
        return WorkoutSession(
            id = "history-${exercise.id}",
            templateId = null,
            name = "Previous Session",
            date = LocalDateTime.now().minusDays(3),
            durationSeconds = 1800,
            exercises = listOf(
                createSessionExercise(
                    sets = sets,
                    exercise = exercise
                ).copy(id = "history-session-ex-${exercise.id}")
            )
        )
    }

    private fun createSet(weight: Double, completed: Boolean, reps: Int = 10): WorkoutSet {
        return WorkoutSet(
            id = "set-${System.nanoTime()}",
            weight = weight,
            reps = reps,
            completed = completed,
            rpe = null,
            rir = null,
            isPr = false
        )
    }
}
