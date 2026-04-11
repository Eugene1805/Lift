package com.eugene.lift.domain.usecase.workout

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.TemplateExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.repository.TemplateRepository
import com.eugene.lift.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test for StartWorkoutFromTemplateUseCase
 * Tests the creation of workout sessions from templates
 */
class StartWorkoutFromTemplateUseCaseTest {

    private lateinit var templateRepository: TemplateRepository
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var useCase: StartWorkoutFromTemplateUseCase

    private val sampleExercise = Exercise(
        id = "exercise-1",
        name = "Bench Press",
        bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS),
        category = ExerciseCategory.BARBELL,
        measureType = MeasureType.REPS_AND_WEIGHT,
        instructions = "",
        imagePath = null
    )

    private val sampleTemplate = WorkoutTemplate(
        id = "template-1",
        name = "Push Day",
        notes = "Chest and triceps workout",
        exercises = listOf(
            TemplateExercise(
                id = "template-ex-1",
                exercise = sampleExercise,
                orderIndex = 0,
                targetSets = 3,
                targetReps = "10",
                restTimerSeconds = 90,
                note = ""
            ),
            TemplateExercise(
                id = "template-ex-2",
                exercise = sampleExercise.copy(id = "exercise-2", name = "Incline Press"),
                orderIndex = 1,
                targetSets = 4,
                targetReps = "8",
                restTimerSeconds = 120,
                note = ""
            )
        )
    )

    @Before
    fun setup() {
        templateRepository = mockk()
        workoutRepository = mockk()
        coEvery { workoutRepository.getLastHistoryForExercise(any(), any()) } returns null
        useCase = StartWorkoutFromTemplateUseCase(templateRepository, workoutRepository)
    }

    @Test
    fun `invoke creates session with correct template data`() = runTest {
        // GIVEN
        coEvery { templateRepository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN
        val result = useCase("template-1")

        // THEN
        assertNotNull(result)
        assertEquals("template-1", result?.templateId)
        assertEquals("Push Day", result?.name)
        assertEquals(2, result?.exercises?.size)
    }

    @Test
    fun `invoke creates correct number of sets for each exercise`() = runTest {
        // GIVEN
        coEvery { templateRepository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN
        val result = useCase("template-1")

        // THEN
        assertNotNull(result)
        assertEquals(3, result?.exercises?.get(0)?.sets?.size) // First exercise has 3 target sets
        assertEquals(4, result?.exercises?.get(1)?.sets?.size) // Second exercise has 4 target sets
    }

    @Test
    fun `invoke creates sets with fallback values when no history exists`() = runTest {
        // GIVEN
        coEvery { templateRepository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN
        val result = useCase("template-1")

        // THEN
        assertNotNull(result)
        val firstExerciseSets = result?.exercises?.get(0)?.sets ?: emptyList()

        // With no history, sets should keep zero/default values.
        assertTrue(firstExerciseSets.all { !it.completed })
        assertTrue(firstExerciseSets.all { it.weight == 0.0 })
        assertTrue(firstExerciseSets.all { it.reps == 0 })
        assertTrue(firstExerciseSets.all { it.rpe == null })
        assertTrue(firstExerciseSets.all { it.rir == null })
        assertTrue(firstExerciseSets.all { !it.isPr })
    }

    @Test
    fun `invoke preserves exercise details from template`() = runTest {
        // GIVEN
        coEvery { templateRepository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN
        val result = useCase("template-1")

        // THEN
        assertNotNull(result)
        val firstSessionExercise = result?.exercises?.get(0)
        assertEquals("Bench Press", firstSessionExercise?.exercise?.name)
        assertEquals(ExerciseCategory.BARBELL, firstSessionExercise?.exercise?.category)
        assertEquals(MeasureType.REPS_AND_WEIGHT, firstSessionExercise?.exercise?.measureType)
    }

    @Test
    fun `invoke returns null when template not found`() = runTest {
        // GIVEN
        coEvery { templateRepository.getTemplate("non-existent") } returns flowOf(null)

        // WHEN
        val result = useCase("non-existent")

        // THEN
        assertNull(result)
    }

    @Test
    fun `invoke generates unique IDs for session and exercises`() = runTest {
        // GIVEN
        coEvery { templateRepository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN
        val result1 = useCase("template-1")
        val result2 = useCase("template-1")

        // THEN
        assertNotNull(result1)
        assertNotNull(result2)

        // Session IDs should be different
        assertTrue(result1!!.id != result2!!.id)

        // Exercise IDs should be different
        assertTrue(result1.exercises[0].id != result2.exercises[0].id)
    }

    @Test
    fun `invoke generates unique IDs for all sets`() = runTest {
        // GIVEN
        coEvery { templateRepository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN
        val result = useCase("template-1")

        // THEN
        assertNotNull(result)
        val allSets = result!!.exercises.flatMap { it.sets }
        val uniqueSetIds = allSets.map { it.id }.distinct()

        // All set IDs should be unique
        assertEquals(allSets.size, uniqueSetIds.size)
    }

    @Test
    fun `invoke creates session with template that has single exercise`() = runTest {
        // GIVEN
        val singleExerciseTemplate = sampleTemplate.copy(
            exercises = listOf(sampleTemplate.exercises.first())
        )
        coEvery { templateRepository.getTemplate("template-1") } returns flowOf(singleExerciseTemplate)

        // WHEN
        val result = useCase("template-1")

        // THEN
        assertNotNull(result)
        assertEquals(1, result?.exercises?.size)
        assertEquals(3, result?.exercises?.first()?.sets?.size)
    }

    @Test
    fun `invoke creates session with template that has many exercises`() = runTest {
        // GIVEN
        val manyExercises = (1..10).map { index ->
            TemplateExercise(
                id = "template-ex-$index",
                exercise = sampleExercise.copy(id = "exercise-$index", name = "Exercise $index"),
                orderIndex = index - 1,
                targetSets = index,
                targetReps = "10",
                restTimerSeconds = 60,
                note = ""
            )
        }
        val largeTemplate = sampleTemplate.copy(exercises = manyExercises)
        coEvery { templateRepository.getTemplate("template-1") } returns flowOf(largeTemplate)

        // WHEN
        val result = useCase("template-1")

        // THEN
        assertNotNull(result)
        assertEquals(10, result?.exercises?.size)

        // Verify each exercise has correct number of sets
        result?.exercises?.forEachIndexed { index, sessionExercise ->
            assertEquals(index + 1, sessionExercise.sets.size)
        }
    }

    @Test
    fun `invoke initializes durationSeconds to zero`() = runTest {
        // GIVEN
        coEvery { templateRepository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN
        val result = useCase("template-1")

        // THEN
        assertNotNull(result)
        assertEquals(0L, result?.durationSeconds)
    }

    @Test
    fun `invoke sets date to current time`() = runTest {
        // GIVEN
        coEvery { templateRepository.getTemplate("template-1") } returns flowOf(sampleTemplate)
        val beforeTime = java.time.LocalDateTime.now()

        // WHEN
        val result = useCase("template-1")

        // THEN
        assertNotNull(result)
        val afterTime = java.time.LocalDateTime.now()

        // Session date should be between before and after
        assertTrue(result!!.date.isAfter(beforeTime.minusSeconds(1)))
        assertTrue(result.date.isBefore(afterTime.plusSeconds(1)))
    }

    @Test
    fun `invoke prefills weights from last session by set index`() = runTest {
        // GIVEN
        coEvery { templateRepository.getTemplate("template-1") } returns flowOf(sampleTemplate)
        val historySession = WorkoutSession(
            id = "history-1",
            templateId = "template-1",
            name = "Push Day",
            date = java.time.LocalDateTime.now().minusDays(1),
            durationSeconds = 1200,
            exercises = listOf(
                SessionExercise(
                    id = "hist-ex-1",
                    exercise = sampleExercise,
                    sets = listOf(
                        WorkoutSet(id = "s1", weight = 80.0, reps = 10, completed = true),
                        WorkoutSet(id = "s2", weight = 82.5, reps = 8, completed = true)
                    )
                )
            )
        )
        coEvery { workoutRepository.getLastHistoryForExercise("exercise-1", "template-1") } returns historySession
        coEvery { workoutRepository.getLastHistoryForExercise("exercise-2", "template-1") } returns null

        // WHEN
        val result = useCase("template-1")

        // THEN
        assertNotNull(result)
        val firstExerciseSets = result!!.exercises.first().sets
        assertEquals(80.0, firstExerciseSets[0].weight, 0.0)
        assertEquals(82.5, firstExerciseSets[1].weight, 0.0)
        assertEquals(0.0, firstExerciseSets[2].weight, 0.0)
    }
}
