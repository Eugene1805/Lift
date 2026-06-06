package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.ExerciseSource
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.repository.ExerciseRepository
import com.eugene.lift.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
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
        useCase = GetExercisesUseCase(repository, workoutRepository, ExerciseCatalogReconciler())
    }

    @Test
    fun `invoke with empty filter returns all exercises from repository`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val emptyFilter = ExerciseFilter()

        // WHEN
        val result = useCase(emptyFilter).first()

        // THEN
        Assert.assertEquals(3, result.size)
        Assert.assertTrue(result.any { it.name == "Bench Press" })
        Assert.assertTrue(result.any { it.name == "Squat" })
        Assert.assertTrue(result.any { it.name == "Push-ups" })
    }

    @Test
    fun `invoke filters by query string`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val filter = ExerciseFilter(query = "press")

        // WHEN
        val result = useCase(filter).first()

        // THEN
        Assert.assertEquals(1, result.size)
        Assert.assertTrue(result.all { it.name.contains("press", ignoreCase = true) })
        Assert.assertTrue(result.any { it.name == "Bench Press" })
    }

    @Test
    fun `invoke filters by body part`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val filter = ExerciseFilter(bodyParts = setOf(BodyPart.CHEST))

        // WHEN
        val result = useCase(filter).first()

        // THEN
        Assert.assertEquals(2, result.size)
        Assert.assertTrue(result.all { it.bodyParts.contains(BodyPart.CHEST) })
    }

    @Test
    fun `invoke filters by category`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val filter = ExerciseFilter(categories = setOf(ExerciseCategory.BARBELL))

        // WHEN
        val result = useCase(filter).first()

        // THEN
        Assert.assertEquals(2, result.size)
        Assert.assertTrue(result.all { it.category == ExerciseCategory.BARBELL })
    }

    @Test
    fun `invoke sorts by name ascending`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val filter = ExerciseFilter(sortOrder = SortOrder.NAME_ASC)

        // WHEN
        val result = useCase(filter).first()

        // THEN
        Assert.assertEquals("Bench Press", result[0].name)
        Assert.assertEquals("Push-ups", result[1].name)
        Assert.assertEquals("Squat", result[2].name)
    }

    @Test
    fun `invoke sorts by name descending`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(sampleExercises)
        val filter = ExerciseFilter(sortOrder = SortOrder.NAME_DESC)

        // WHEN
        val result = useCase(filter).first()

        // THEN
        Assert.assertEquals("Squat", result[0].name)
        Assert.assertEquals("Push-ups", result[1].name)
        Assert.assertEquals("Bench Press", result[2].name)
    }

    @Test
    fun `invoke returns empty list when repository has no exercises`() = runTest {
        // GIVEN
        coEvery { repository.getExercises() } returns flowOf(emptyList())
        val filter = ExerciseFilter()

        // WHEN
        val result = useCase(filter).first()

        // THEN
        Assert.assertEquals(0, result.size)
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
        Assert.assertEquals(1, result.size)
        Assert.assertEquals("Bench Press", result[0].name)
    }

    @Test
    fun `invoke collapses remote duplicates while preserving stable local id`() = runTest {
        val local = Exercise(
            id = "local-bench",
            name = "Bench Press",
            bodyParts = listOf(BodyPart.CHEST),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "",
            imagePath = null
        )
        val remote = Exercise(
            id = "remote-bench",
            name = "bench-press",
            bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS),
            category = ExerciseCategory.MACHINE,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "Drive through the bar.",
            imagePath = "https://example.com/bench.png",
            remoteId = 42,
            source = ExerciseSource.WGER,
            lastSyncedAt = 123L,
            syncVersion = 1
        )
        coEvery { repository.getExercises() } returns flowOf(
            listOf(
                sampleExercises[1],
                sampleExercises[2],
                local,
                remote
            )
        )

        val result = useCase(ExerciseFilter()).first()
        val bench = result.single { it.name.equals("Bench Press", ignoreCase = true) }

        Assert.assertEquals(3, result.size)
        Assert.assertEquals("local-bench", bench.id)
        Assert.assertEquals(42, bench.remoteId)
        Assert.assertEquals(ExerciseSource.WGER, bench.source)
        Assert.assertEquals("https://example.com/bench.png", bench.imagePath)
        Assert.assertEquals(setOf(BodyPart.CHEST, BodyPart.TRICEPS), bench.bodyParts.toSet())
    }

    @Test
    fun `invoke keeps local exercises with matching names visible when no remote exists`() = runTest {
        val first = Exercise(
            id = "local-1",
            name = "Bench Press",
            bodyParts = listOf(BodyPart.CHEST),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "",
            imagePath = null
        )
        val second = Exercise(
            id = "local-2",
            name = "bench-press",
            bodyParts = listOf(BodyPart.TRICEPS),
            category = ExerciseCategory.MACHINE,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "",
            imagePath = null
        )
        coEvery { repository.getExercises() } returns flowOf(listOf(first, second))

        val result = useCase(ExerciseFilter()).first()

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(setOf("local-1", "local-2"), result.map { it.id }.toSet())
    }
}
