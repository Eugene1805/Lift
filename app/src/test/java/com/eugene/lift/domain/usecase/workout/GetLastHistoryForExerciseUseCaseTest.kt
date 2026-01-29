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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit test for GetLastHistoryForExerciseUseCase
 * Tests retrieval of exercise history for ghost data display
 */
class GetLastHistoryForExerciseUseCaseTest {

    private lateinit var repository: WorkoutRepository
    private lateinit var useCase: GetLastHistoryForExerciseUseCase

    private val sampleExercise = Exercise(
        id = "exercise-1",
        name = "Bench Press",
        bodyParts = listOf(BodyPart.CHEST),
        category = ExerciseCategory.BARBELL,
        measureType = MeasureType.REPS_AND_WEIGHT,
        instructions = "",
        imagePath = null
    )

    private val sampleSession = WorkoutSession(
        id = "session-1",
        templateId = "template-1",
        name = "Push Day",
        date = LocalDateTime.now().minusDays(3),
        durationSeconds = 3600,
        exercises = listOf(
            SessionExercise(
                id = "session-ex-1",
                exercise = sampleExercise,
                sets = listOf(
                    WorkoutSet(
                        id = "set-1",
                        weight = 80.0,
                        reps = 10,
                        completed = true,
                        rpe = 7.0,
                        rir = 3,
                        isPr = false
                    ),
                    WorkoutSet(
                        id = "set-2",
                        weight = 80.0,
                        reps = 9,
                        completed = true,
                        rpe = 8.0,
                        rir = 2,
                        isPr = false
                    )
                )
            )
        )
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetLastHistoryForExerciseUseCase(repository)
    }

    @Test
    fun `invoke returns last session for exercise`() = runTest {
        // GIVEN
        coEvery { repository.getLastHistoryForExercise("exercise-1") } returns sampleSession

        // WHEN
        val result = useCase("exercise-1")

        // THEN
        assertNotNull(result)
        assertEquals("session-1", result?.id)
        assertEquals("Push Day", result?.name)
    }

    @Test
    fun `invoke returns session with exercise data`() = runTest {
        // GIVEN
        coEvery { repository.getLastHistoryForExercise("exercise-1") } returns sampleSession

        // WHEN
        val result = useCase("exercise-1")

        // THEN
        assertNotNull(result)
        val sessionExercise = result?.exercises?.firstOrNull()
        assertNotNull(sessionExercise)
        assertEquals("Bench Press", sessionExercise?.exercise?.name)
        assertEquals(2, sessionExercise?.sets?.size)
    }

    @Test
    fun `invoke returns session with set details for ghost data`() = runTest {
        // GIVEN
        coEvery { repository.getLastHistoryForExercise("exercise-1") } returns sampleSession

        // WHEN
        val result = useCase("exercise-1")

        // THEN
        assertNotNull(result)
        val sets = result?.exercises?.firstOrNull()?.sets
        assertNotNull(sets)
        assertEquals(2, sets?.size)

        val firstSet = sets?.firstOrNull()
        assertEquals(80.0, firstSet?.weight)
        assertEquals(10, firstSet?.reps)
        assertEquals(7.0, firstSet?.rpe)
        assertEquals(3, firstSet?.rir)
    }

    @Test
    fun `invoke returns null when no history exists for exercise`() = runTest {
        // GIVEN
        coEvery { repository.getLastHistoryForExercise("non-existent") } returns null

        // WHEN
        val result = useCase("non-existent")

        // THEN
        assertNull(result)
    }

    @Test
    fun `invoke calls repository with correct exercise ID`() = runTest {
        // GIVEN
        coEvery { repository.getLastHistoryForExercise("exercise-1") } returns sampleSession

        // WHEN
        useCase("exercise-1")

        // THEN
        coVerify(exactly = 1) { repository.getLastHistoryForExercise("exercise-1") }
    }

    @Test
    fun `invoke handles different exercises independently`() = runTest {
        // GIVEN
        val exercise2 = sampleExercise.copy(id = "exercise-2", name = "Squat")
        val session2 = sampleSession.copy(
            id = "session-2",
            exercises = listOf(
                SessionExercise(
                    id = "session-ex-2",
                    exercise = exercise2,
                    sets = listOf(
                        WorkoutSet(
                            id = "set-3",
                            weight = 100.0,
                            reps = 5,
                            completed = true,
                            rpe = 9.0,
                            rir = 1,
                            isPr = true
                        )
                    )
                )
            )
        )

        coEvery { repository.getLastHistoryForExercise("exercise-1") } returns sampleSession
        coEvery { repository.getLastHistoryForExercise("exercise-2") } returns session2

        // WHEN
        val result1 = useCase("exercise-1")
        val result2 = useCase("exercise-2")

        // THEN
        assertNotNull(result1)
        assertNotNull(result2)
        assertEquals("Bench Press", result1?.exercises?.first()?.exercise?.name)
        assertEquals("Squat", result2?.exercises?.first()?.exercise?.name)
        assertEquals(80.0, result1?.exercises?.first()?.sets?.first()?.weight)
        assertEquals(100.0, result2?.exercises?.first()?.sets?.first()?.weight)
    }

    @Test
    fun `invoke returns most recent session when multiple exist`() = runTest {
        // GIVEN - Repository should return the most recent one
        val recentSession = sampleSession.copy(
            date = LocalDateTime.now().minusDays(1)
        )
        coEvery { repository.getLastHistoryForExercise("exercise-1") } returns recentSession

        // WHEN
        val result = useCase("exercise-1")

        // THEN
        assertNotNull(result)
        // The date should be the recent one (1 day ago, not 3 days ago)
        val daysDiff = java.time.Duration.between(result!!.date, LocalDateTime.now()).toDays()
        assertEquals(1, daysDiff)
    }

    @Test
    fun `invoke handles session with empty sets`() = runTest {
        // GIVEN
        val sessionWithNoSets = sampleSession.copy(
            exercises = listOf(
                SessionExercise(
                    id = "session-ex-1",
                    exercise = sampleExercise,
                    sets = emptyList()
                )
            )
        )
        coEvery { repository.getLastHistoryForExercise("exercise-1") } returns sessionWithNoSets

        // WHEN
        val result = useCase("exercise-1")

        // THEN
        assertNotNull(result)
        val sets = result?.exercises?.firstOrNull()?.sets
        assertNotNull(sets)
        assertEquals(0, sets?.size)
    }

    @Test
    fun `invoke handles session with multiple exercises`() = runTest {
        // GIVEN
        val multiExerciseSession = sampleSession.copy(
            exercises = listOf(
                sampleSession.exercises.first(),
                SessionExercise(
                    id = "session-ex-2",
                    exercise = sampleExercise.copy(id = "exercise-2", name = "Squat"),
                    sets = listOf(
                        WorkoutSet(
                            id = "set-3",
                            weight = 100.0,
                            reps = 5,
                            completed = true,
                            rpe = null,
                            rir = null,
                            isPr = false
                        )
                    )
                )
            )
        )
        coEvery { repository.getLastHistoryForExercise("exercise-1") } returns multiExerciseSession

        // WHEN
        val result = useCase("exercise-1")

        // THEN
        assertNotNull(result)
        assertEquals(2, result?.exercises?.size)
    }
}
