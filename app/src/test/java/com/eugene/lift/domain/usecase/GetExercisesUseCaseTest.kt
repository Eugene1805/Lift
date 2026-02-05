package com.eugene.lift.domain.usecase

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.repository.ExerciseRepository
import com.eugene.lift.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test for GetExercisesUseCase
 * Tests filtering and retrieval logic
 */
class GetExercisesUseCaseTest {

    private lateinit var repository: ExerciseRepository
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var useCase: GetExercisesUseCase

    private val sampleExercises = listOf(
        Exercise(
            id = "1",
            name = "Bench Press",
            bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "",
            imagePath = null
        ),
        Exercise(
            id = "2",
            name = "Squat",
            bodyParts = listOf(BodyPart.QUADRICEPS, BodyPart.GLUTES),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "",
            imagePath = null
        ),
        Exercise(
            id = "3",
            name = "Push-ups",
            bodyParts = listOf(BodyPart.CHEST),
            category = ExerciseCategory.BODYWEIGHT,
            measureType = MeasureType.REPS_ONLY,
            instructions = "",
            imagePath = null
        )
    )

    @Before
    fun setup() {
        repository = mockk()
        workoutRepository = mockk()
        coEvery { workoutRepository.getExerciseUsageCount() } returns emptyMap()
        coEvery { workoutRepository.getExerciseLastUsedDates() } returns emptyMap()
        useCase = GetExercisesUseCase(repository, workoutRepository)
    }

    @Test
    fun `invoke with empty filter returns all exercises from repository`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val emptyFilter = ExerciseFilter()

        // WHEN
        val result = useCase(emptyFilter).first()

        // THEN
        assertEquals(3, result.size)
        assertTrue(result.any { it.name == "Bench Press" })
        assertTrue(result.any { it.name == "Squat" })
        assertTrue(result.any { it.name == "Push-ups" })
    }

    @Test
    fun `invoke filters by query string`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val filter = ExerciseFilter(query = "press")

        // WHEN
        val result = useCase(filter).first()

        // THEN
        assertEquals(1, result.size)
        assertTrue(result.all { it.name.contains("press", ignoreCase = true) })
        assertTrue(result.any { it.name == "Bench Press" })
    }

    @Test
    fun `invoke filters by body part`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val filter = ExerciseFilter(bodyParts = setOf(BodyPart.CHEST))

        // WHEN
        val result = useCase(filter).first()

        // THEN
        assertEquals(2, result.size)
        assertTrue(result.all { it.bodyParts.contains(BodyPart.CHEST) })
    }

    @Test
    fun `invoke filters by category`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val filter = ExerciseFilter(categories = setOf(ExerciseCategory.BARBELL))

        // WHEN
        val result = useCase(filter).first()

        // THEN
        assertEquals(2, result.size)
        assertTrue(result.all { it.category == ExerciseCategory.BARBELL })
    }

    @Test
    fun `invoke sorts by name ascending`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val filter = ExerciseFilter(sortOrder = SortOrder.NAME_ASC)

        // WHEN
        val result = useCase(filter).first()

        // THEN
        assertEquals("Bench Press", result[0].name)
        assertEquals("Push-ups", result[1].name)
        assertEquals("Squat", result[2].name)
    }

    @Test
    fun `invoke sorts by name descending`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val filter = ExerciseFilter(sortOrder = SortOrder.NAME_DESC)

        // WHEN
        val result = useCase(filter).first()

        // THEN
        assertEquals("Squat", result[0].name)
        assertEquals("Push-ups", result[1].name)
        assertEquals("Bench Press", result[2].name)
    }

    @Test
    fun `invoke returns empty list when repository has no exercises`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(emptyList())
        val filter = ExerciseFilter()

        // WHEN
        val result = useCase(filter).first()

        // THEN
        assertEquals(0, result.size)
    }

    @Test
    fun `invoke combines multiple filters`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val filter = ExerciseFilter(
            query = "press",
            bodyParts = setOf(BodyPart.CHEST),
            categories = setOf(ExerciseCategory.BARBELL)
        )

        // WHEN
        val result = useCase(filter).first()

        // THEN
        assertEquals(1, result.size)
        assertEquals("Bench Press", result[0].name)
    }
}
