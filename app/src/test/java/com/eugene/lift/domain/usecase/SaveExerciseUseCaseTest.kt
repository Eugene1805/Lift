package com.eugene.lift.domain.usecase

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.repository.ExerciseRepository
import com.eugene.lift.domain.usecase.exercise.SaveExerciseUseCase
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
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
        useCase = SaveExerciseUseCase(repository)
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

    @Test(expected = IllegalArgumentException::class)
    fun `invoke with blank name throws IllegalArgumentException`() = runTest {
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
        useCase(exercise)

        // THEN - Exception should be thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke with empty name throws IllegalArgumentException`() = runTest {
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
        useCase(exercise)

        // THEN - Exception should be thrown
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
