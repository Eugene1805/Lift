package com.eugene.lift.domain.usecase.history

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit test for GetWorkoutHistoryUseCase
 * Tests retrieval of workout history
 */
class GetWorkoutHistoryUseCaseTest {

    private lateinit var repository: WorkoutRepository
    private lateinit var useCase: GetWorkoutHistoryUseCase

    private val sampleExercise = Exercise(
        id = "exercise-1",
        name = "Bench Press",
        bodyParts = listOf(BodyPart.CHEST),
        category = ExerciseCategory.BARBELL,
        measureType = MeasureType.REPS_AND_WEIGHT,
        instructions = "",
        imagePath = null
    )

    private val sampleSessions = listOf(
        WorkoutSession(
            id = "session-1",
            templateId = "template-1",
            name = "Push Day",
            date = LocalDateTime.now().minusDays(1),
            durationSeconds = 3600,
            exercises = listOf(
                SessionExercise(
                    id = "ex-1",
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
                        )
                    )
                )
            )
        ),
        WorkoutSession(
            id = "session-2",
            templateId = null,
            name = "Quick Workout",
            date = LocalDateTime.now().minusDays(3),
            durationSeconds = 2400,
            exercises = emptyList()
        ),
        WorkoutSession(
            id = "session-3",
            templateId = "template-2",
            name = "Pull Day",
            date = LocalDateTime.now().minusWeeks(1),
            durationSeconds = 4200,
            exercises = listOf(
                SessionExercise(
                    id = "ex-2",
                    exercise = sampleExercise.copy(id = "exercise-2", name = "Pull-ups"),
                    sets = listOf(
                        WorkoutSet(
                            id = "set-2",
                            weight = 0.0,
                            reps = 12,
                            completed = true,
                            rpe = 8.0,
                            rir = 2,
                            isPr = true
                        )
                    )
                )
            )
        )
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetWorkoutHistoryUseCase(repository)
    }

    @Test
    fun `invoke returns all workout sessions from repository`() = runTest {
        // GIVEN
        coEvery { repository.getHistory(null, null) } returns flowOf(sampleSessions)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(3, result.size)
        assertTrue(result.any { it.name == "Push Day" })
        assertTrue(result.any { it.name == "Quick Workout" })
        assertTrue(result.any { it.name == "Pull Day" })
    }

    @Test
    fun `invoke returns empty list when no history exists`() = runTest {
        // GIVEN
        coEvery { repository.getHistory(null, null) } returns flowOf(emptyList())

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(0, result.size)
    }

    @Test
    fun `invoke calls repository with null parameters`() = runTest {
        // GIVEN
        coEvery { repository.getHistory(null, null) } returns flowOf(sampleSessions)

        // WHEN
        useCase().first()

        // THEN
        coVerify(exactly = 1) { repository.getHistory(null, null) }
    }

    @Test
    fun `invoke returns sessions with all properties`() = runTest {
        // GIVEN
        coEvery { repository.getHistory(null, null) } returns flowOf(sampleSessions)

        // WHEN
        val result = useCase().first()

        // THEN
        val session = result.first()
        assertEquals("session-1", session.id)
        assertEquals("Push Day", session.name)
        assertEquals("template-1", session.templateId)
        assertEquals(3600L, session.durationSeconds)
        assertEquals(1, session.exercises.size)
    }

    @Test
    fun `invoke returns sessions with exercise details`() = runTest {
        // GIVEN
        coEvery { repository.getHistory(null, null) } returns flowOf(sampleSessions)

        // WHEN
        val result = useCase().first()

        // THEN
        val sessionExercise = result.first().exercises.first()
        assertEquals("Bench Press", sessionExercise.exercise.name)
        assertEquals(1, sessionExercise.sets.size)
        assertEquals(80.0, sessionExercise.sets.first().weight, 0.01)
    }

    @Test
    fun `invoke returns sessions with set details`() = runTest {
        // GIVEN
        coEvery { repository.getHistory(null, null) } returns flowOf(sampleSessions)

        // WHEN
        val result = useCase().first()

        // THEN
        val set = result.first().exercises.first().sets.first()
        assertEquals(80.0, set.weight, 0.01)
        assertEquals(10, set.reps)
        assertEquals(true, set.completed)
        assertEquals(7.0, set.rpe ?: 0.0, 0.01)
        assertEquals(3, set.rir)
        assertEquals(false, set.isPr)
    }

    @Test
    fun `invoke returns sessions ordered by date from repository`() = runTest {
        // GIVEN
        coEvery { repository.getHistory(null, null) } returns flowOf(sampleSessions)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals("session-1", result[0].id) // Most recent
        assertEquals("session-2", result[1].id)
        assertEquals("session-3", result[2].id) // Oldest
    }

    @Test
    fun `invoke handles sessions without template`() = runTest {
        // GIVEN
        val sessionsWithoutTemplate = listOf(
            sampleSessions[1] // Quick Workout has no template
        )
        coEvery { repository.getHistory(null, null) } returns flowOf(sessionsWithoutTemplate)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(1, result.size)
        assertEquals("Quick Workout", result.first().name)
        assertEquals(null, result.first().templateId)
    }

    @Test
    fun `invoke handles sessions with PR sets`() = runTest {
        // GIVEN
        coEvery { repository.getHistory(null, null) } returns flowOf(sampleSessions)

        // WHEN
        val result = useCase().first()

        // THEN
        val pullDaySession = result.last()
        val prSet = pullDaySession.exercises.first().sets.first()
        assertTrue(prSet.isPr)
    }

    @Test
    fun `invoke returns flow that emits multiple times`() = runTest {
        // GIVEN
        val flow = kotlinx.coroutines.flow.flow {
            emit(listOf(sampleSessions[0]))
            kotlinx.coroutines.delay(100)
            emit(sampleSessions)
        }
        coEvery { repository.getHistory(null, null) } returns flow

        // WHEN
        val results = mutableListOf<List<WorkoutSession>>()
        useCase().collect { results.add(it) }

        // THEN
        assertEquals(2, results.size)
        assertEquals(1, results[0].size)
        assertEquals(3, results[1].size)
    }

    @Test
    fun `invoke handles large history`() = runTest {
        // GIVEN
        val manySessions = (1..100).map { index ->
            WorkoutSession(
                id = "session-$index",
                templateId = null,
                name = "Workout $index",
                date = LocalDateTime.now().minusDays(index.toLong()),
                durationSeconds = 3600,
                exercises = emptyList()
            )
        }
        coEvery { repository.getHistory(null, null) } returns flowOf(manySessions)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(100, result.size)
    }

    @Test
    fun `invoke handles sessions with empty exercises`() = runTest {
        // GIVEN
        val emptySession = WorkoutSession(
            id = "session-1",
            templateId = null,
            name = "Empty Session",
            date = LocalDateTime.now(),
            durationSeconds = 0,
            exercises = emptyList()
        )
        coEvery { repository.getHistory(null, null) } returns flowOf(listOf(emptySession))

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(1, result.size)
        assertEquals(0, result.first().exercises.size)
    }

    @Test
    fun `invoke handles sessions with multiple exercises`() = runTest {
        // GIVEN
        val multiExerciseSession = WorkoutSession(
            id = "session-1",
            templateId = "template-1",
            name = "Full Body",
            date = LocalDateTime.now(),
            durationSeconds = 5400,
            exercises = (1..5).map { index ->
                SessionExercise(
                    id = "ex-$index",
                    exercise = sampleExercise.copy(id = "exercise-$index", name = "Exercise $index"),
                    sets = listOf(
                        WorkoutSet(
                            id = "set-$index",
                            weight = 50.0,
                            reps = 10,
                            completed = true,
                            rpe = null,
                            rir = null,
                            isPr = false
                        )
                    )
                )
            }
        )
        coEvery { repository.getHistory(null, null) } returns flowOf(listOf(multiExerciseSession))

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(1, result.size)
        assertEquals(5, result.first().exercises.size)
    }
}
