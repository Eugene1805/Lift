package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.repository.ExerciseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AssignMissingImagesUseCaseTest {

    private lateinit var repository: ExerciseRepository
    private lateinit var imageResolver: ExerciseImageResolver
    private lateinit var useCase: AssignMissingImagesUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        imageResolver = mockk()
        useCase = AssignMissingImagesUseCase(repository, imageResolver)
    }

    private fun exercise(name: String, id: String = name) = Exercise(
        id = id,
        name = name,
        category = ExerciseCategory.BARBELL,
        measureType = MeasureType.REPS_AND_WEIGHT,
        instructions = "",
        imagePath = null,
        bodyParts = emptyList<BodyPart>()
    )

    @Test
    fun `invoke updates imagePath for exercises with a known mapping`() = runTest {
        // GIVEN
        val bench = exercise("Bench Press (Barbell)", id = "bench_id")
        coEvery { repository.getExercisesWithoutImage() } returns listOf(bench)
        every { imageResolver.resolveDrawable("Bench Press (Barbell)") } returns "bench_press"

        // WHEN
        useCase()

        // THEN
        coVerify(exactly = 1) { repository.updateImagePath("bench_id", "bench_press") }
    }

    @Test
    fun `invoke does NOT call updateImagePath for exercises with no mapping`() = runTest {
        // GIVEN
        val unknown = exercise("Unknown Exercise XYZ", id = "unknown_id")
        coEvery { repository.getExercisesWithoutImage() } returns listOf(unknown)
        every { imageResolver.resolveDrawable("Unknown Exercise XYZ") } returns null

        // WHEN
        useCase()

        // THEN - no update should happen for exercises not in the mapper
        coVerify(exactly = 0) { repository.updateImagePath(any(), any()) }
    }

    @Test
    fun `invoke handles mixed exercises - only updates those with known mappings`() = runTest {
        // GIVEN
        val bench = exercise("Bench Press (Barbell)", id = "bench_id")
        val unknown = exercise("Some Obscure Exercise", id = "unknown_id")
        val deadlift = exercise("Deadlift (Barbell)", id = "dead_id")
        coEvery { repository.getExercisesWithoutImage() } returns listOf(bench, unknown, deadlift)
        every { imageResolver.resolveDrawable("Bench Press (Barbell)") } returns "bench_press"
        every { imageResolver.resolveDrawable("Some Obscure Exercise") } returns null
        every { imageResolver.resolveDrawable("Deadlift (Barbell)") } returns "deadlift"

        // WHEN
        useCase()

        // THEN
        coVerify(exactly = 1) { repository.updateImagePath("bench_id", "bench_press") }
        coVerify(exactly = 1) { repository.updateImagePath("dead_id", "deadlift") }
        coVerify(exactly = 0) { repository.updateImagePath("unknown_id", any()) }
    }

    @Test
    fun `invoke does nothing when no exercises are missing images`() = runTest {
        // GIVEN
        coEvery { repository.getExercisesWithoutImage() } returns emptyList()

        // WHEN
        useCase()

        // THEN
        coVerify(exactly = 0) { repository.updateImagePath(any(), any()) }
        verify(exactly = 0) { imageResolver.resolveDrawable(any()) }
    }
}
