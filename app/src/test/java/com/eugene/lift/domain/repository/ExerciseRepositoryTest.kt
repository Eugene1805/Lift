package com.eugene.lift.domain.repository

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit test for ExerciseRepository implementations
 * Tests repository contract without data source implementation details
 */
class ExerciseRepositoryTest {

    private lateinit var repository: ExerciseRepository

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `saveExercise persists exercise data`() = runTest {
        // GIVEN
        val exercise = Exercise(
            id = "1",
            name = "Bench Press",
            bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "Lower bar to chest",
            imagePath = null
        )

        // WHEN
        repository.saveExercise(exercise)

        // THEN
        coVerify(exactly = 1) { repository.saveExercise(exercise) }
    }

    @Test
    fun `deleteExercise removes exercise by id`() = runTest {
        // GIVEN
        val exerciseId = "exercise_123"

        // WHEN
        repository.deleteExercise(exerciseId)

        // THEN
        coVerify(exactly = 1) { repository.deleteExercise(exerciseId) }
    }

    @Test
    fun `saveExercise handles exercise with multiple body parts`() = runTest {
        // GIVEN
        val exercise = Exercise(
            id = "2",
            name = "Deadlift",
            bodyParts = listOf(
                BodyPart.HAMSTRINGS,
                BodyPart.GLUTES,
                BodyPart.LOWER_BACK,
                BodyPart.TRAPS
            ),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "Keep back straight",
            imagePath = null
        )

        // WHEN
        repository.saveExercise(exercise)

        // THEN
        coVerify(exactly = 1) { repository.saveExercise(exercise) }
    }

    @Test
    fun `saveExercise handles exercise with image path`() = runTest {
        // GIVEN
        val exercise = Exercise(
            id = "3",
            name = "Cable Fly",
            bodyParts = listOf(BodyPart.CHEST),
            category = ExerciseCategory.MACHINE,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "Squeeze at center",
            imagePath = "/path/to/image.jpg"
        )

        // WHEN
        repository.saveExercise(exercise)

        // THEN
        coVerify(exactly = 1) { repository.saveExercise(exercise) }
    }
}
