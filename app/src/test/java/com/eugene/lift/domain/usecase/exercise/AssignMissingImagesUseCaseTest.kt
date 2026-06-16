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
import kotlinx.coroutines.flow.flowOf
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
        every { repository.getExercises() } returns flowOf(listOf(bench))
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
        every { repository.getExercises() } returns flowOf(listOf(unknown))
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
        every { repository.getExercises() } returns flowOf(listOf(bench, unknown, deadlift))
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
    fun `invoke does nothing when there are no exercises to repair`() = runTest {
        // GIVEN
        every { repository.getExercises() } returns flowOf(emptyList())

        // WHEN
        useCase()

        // THEN
        coVerify(exactly = 0) { repository.updateImagePath(any(), any()) }
        verify(exactly = 0) { imageResolver.resolveDrawable(any()) }
    }

    @Test
    fun `invoke prefers seedKey mapping when present`() = runTest {
        val seeded = exercise("Whatever", id = "seeded_id").copy(
            seedKey = "seed_smith_machine_hip_thrust"
        )
        every { repository.getExercises() } returns flowOf(listOf(seeded))
        every { imageResolver.resolveDrawableForSeedKey("seed_smith_machine_hip_thrust") } returns "smith_machine_hip_thrust"

        useCase()

        coVerify(exactly = 1) { repository.updateImagePath("seeded_id", "smith_machine_hip_thrust") }
        verify(exactly = 0) { imageResolver.resolveDrawable(any()) }
    }

    @Test
    fun `invoke repairs stale seeded image paths`() = runTest {
        val seeded = exercise("Weighted Dips", id = "weighted_dips_id").copy(
            imagePath = "weigthed_dips",
            seedKey = "seed_weighted_dip"
        )
        every { repository.getExercises() } returns flowOf(listOf(seeded))
        every { imageResolver.resolveDrawableForSeedKey("seed_weighted_dip") } returns "weighted_dips"

        useCase()

        coVerify(exactly = 1) { repository.updateImagePath("weighted_dips_id", "weighted_dips") }
        verify(exactly = 0) { imageResolver.resolveDrawable(any()) }
    }
}
