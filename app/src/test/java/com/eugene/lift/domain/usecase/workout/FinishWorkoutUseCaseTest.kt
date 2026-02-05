package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
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
        val userProfileRepository = mockk<com.eugene.lift.domain.repository.UserProfileRepository>(relaxed = true)
        coEvery { userProfileRepository.getCurrentProfileOnce() } returns null
        useCase = FinishWorkoutUseCase(repository, userProfileRepository)
    }

    @Test
    fun `invoke saves session with calculated duration`() = runTest {
        // GIVEN
        val startTime = LocalDateTime.now().minusMinutes(30)
        val session = createSampleSession(startTime)
        coEvery { repository.getPersonalRecord(any()) } returns flowOf(null)

        // WHEN
        useCase(session)

        // THEN
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
        val previousRecord = WorkoutSet(
            id = "previous-set",
            weight = 90.0,
            reps = 10,
            completed = true,
            isPr = true
        )
        coEvery { repository.getPersonalRecord("exercise-1") } returns flowOf(previousRecord)

        // WHEN
        useCase(session)

        // THEN
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
        val previousRecord = WorkoutSet(
            id = "previous-set",
            weight = 100.0,
            reps = 10,
            completed = true,
            isPr = true
        )
        coEvery { repository.getPersonalRecord("exercise-1") } returns flowOf(previousRecord)

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
        val previousRecord = WorkoutSet(
            id = "previous-set",
            weight = 100.0,
            reps = 10,
            completed = true,
            isPr = true
        )
        coEvery { repository.getPersonalRecord("exercise-1") } returns flowOf(previousRecord)

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
        coEvery { repository.getPersonalRecord("exercise-1") } returns flowOf(null)

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
        val previousRecord = WorkoutSet(
            id = "previous-set",
            weight = 95.0,
            reps = 10,
            completed = true,
            isPr = true
        )
        coEvery { repository.getPersonalRecord("exercise-1") } returns flowOf(previousRecord)

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
        val previousRecord = WorkoutSet(
            id = "previous-set",
            weight = 95.0,
            reps = 10,
            completed = true,
            isPr = true
        )
        coEvery { repository.getPersonalRecord("exercise-1") } returns flowOf(previousRecord)

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
        coEvery { repository.getPersonalRecord("exercise-1") } returns flowOf(null)

        // WHEN
        useCase(session)

        // THEN
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        val savedSets = savedSession.exercises.first().sets

        assertFalse("Incomplete set should NOT be PR even with higher weight", savedSets[0].isPr)
        assertTrue("Completed set should be PR", savedSets[1].isPr)
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

    @Test(expected = IllegalStateException::class)
    fun `invoke throws exception when workout has no completed exercises`() = runTest {
        // GIVEN - All exercises have no sets
        val emptyExercise = SessionExercise(
            id = "session-ex-1",
            exercise = sampleExercise,
            sets = emptyList()
        )
        val session = createSampleSession(LocalDateTime.now()).copy(
            exercises = listOf(emptyExercise)
        )

        // WHEN - Should throw exception
        useCase(session)

        // THEN - Exception expected
    }

    @Test
    fun `invoke handles exercise with only incomplete sets`() = runTest {
        // GIVEN - Exercise with only incomplete sets
        val sets = listOf(
            createSet(weight = 100.0, completed = false),
            createSet(weight = 90.0, completed = false)
        )
        val session = createSessionWithSets(sets)
        coEvery { repository.getPersonalRecord("exercise-1") } returns flowOf(null)

        // WHEN
        useCase(session)

        // THEN
        val slot = slot<WorkoutSession>()
        coVerify { repository.saveSession(capture(slot)) }

        val savedSession = slot.captured
        val savedSets = savedSession.exercises.first().sets

        // All sets should remain but none marked as PR
        assertEquals(2, savedSets.size)
        assertTrue(savedSets.none { it.isPr })
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

        coEvery { repository.getPersonalRecord("exercise-1") } returns flowOf(
            WorkoutSet(id = "pr-1", weight = 95.0, reps = 10, completed = true, isPr = true)
        )
        coEvery { repository.getPersonalRecord("exercise-2") } returns flowOf(
            WorkoutSet(id = "pr-2", weight = 140.0, reps = 10, completed = true, isPr = true)
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

    private fun createSessionWithSet(weight: Double, completed: Boolean): WorkoutSession {
        return createSampleSession(LocalDateTime.now()).copy(
            exercises = listOf(
                createSessionExercise(listOf(createSet(weight, completed)))
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

    private fun createSet(weight: Double, completed: Boolean): WorkoutSet {
        return WorkoutSet(
            id = "set-${System.nanoTime()}",
            weight = weight,
            reps = 10,
            completed = completed,
            rpe = null,
            rir = null,
            isPr = false
        )
    }
}
