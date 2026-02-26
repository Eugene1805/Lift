package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.core.util.SafeExecutor
import com.eugene.lift.domain.error.AppError
import com.eugene.lift.domain.error.AppResult
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.repository.ExerciseRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Unit test for SaveExerciseUseCase
 * Tests business logic without Android dependencies
 */
class SaveExerciseUseCaseTest {

    private lateinit var repository: ExerciseRepository
    private lateinit var useCase: SaveExerciseUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = SaveExerciseUseCase(repository, SafeExecutor(logger = null))
    }

    @Test
    fun `invoke with valid exercise saves to repository`() = runTest {
        // GIVEN
        val exercise = Exercise(
            id = "123",
            name = "Bench Press",
            bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "Test instructions",
            imagePath = null
        )

        // WHEN
        useCase(exercise)

        // THEN
        coVerify(exactly = 1) { repository.saveExercise(exercise) }
    }

    @Test
    fun `invoke with blank name returns Validation error`() = runTest {
        // GIVEN
        val exercise = Exercise(
            id = "123",
            name = "   ", // Blank name
            bodyParts = listOf(BodyPart.CHEST),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "",
            imagePath = null
        )

        // WHEN
        val result = useCase(exercise)

        // THEN
        Assert.assertTrue(result is AppResult.Error)
        Assert.assertEquals(AppError.Validation, (result as AppResult.Error).error)
        coVerify(exactly = 0) { repository.saveExercise(any()) }
    }

    @Test
    fun `invoke with empty name returns Validation error`() = runTest {
        // GIVEN
        val exercise = Exercise(
            id = "123",
            name = "", // Empty name
            bodyParts = listOf(BodyPart.CHEST),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "",
            imagePath = null
        )

        // WHEN
        val result = useCase(exercise)

        // THEN
        Assert.assertTrue(result is AppResult.Error)
        Assert.assertEquals(AppError.Validation, (result as AppResult.Error).error)
        coVerify(exactly = 0) { repository.saveExercise(any()) }
    }

    @Test
    fun `invoke with multiple body parts saves correctly`() = runTest {
        // GIVEN
        val exercise = Exercise(
            id = "456",
            name = "Deadlift",
            bodyParts = listOf(BodyPart.HAMSTRINGS, BodyPart.GLUTES, BodyPart.LOWER_BACK),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "Keep back straight",
            imagePath = null
        )

        // WHEN
        useCase(exercise)

        // THEN
        coVerify(exactly = 1) { repository.saveExercise(exercise) }
    }
}