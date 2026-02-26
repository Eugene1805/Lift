package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.repository.ExerciseRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Unit test for GetExerciseDetailUseCase
 * Tests retrieval of exercise details by ID
 */
class GetExerciseDetailUseCaseTest {

    private lateinit var repository: ExerciseRepository
    private lateinit var useCase: GetExerciseDetailUseCase

    private val sampleExercise = Exercise(
        id = "exercise-1",
        name = "Bench Press",
        bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS),
        category = ExerciseCategory.BARBELL,
        measureType = MeasureType.REPS_AND_WEIGHT,
        instructions = "Lie on bench, lower bar to chest, press up",
        imagePath = "/images/bench_press.jpg"
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetExerciseDetailUseCase(repository)
    }

    @Test
    fun `invoke returns exercise by ID`() = runTest {
        // GIVEN
        coEvery { repository.getExerciseById("exercise-1") } returns flowOf(sampleExercise)

        // WHEN
        val result = useCase("exercise-1").first()

        // THEN
        Assert.assertNotNull(result)
        Assert.assertEquals("exercise-1", result?.id)
        Assert.assertEquals("Bench Press", result?.name)
    }

    @Test
    fun `invoke returns exercise with all properties`() = runTest {
        // GIVEN
        coEvery { repository.getExerciseById("exercise-1") } returns flowOf(sampleExercise)

        // WHEN
        val result = useCase("exercise-1").first()

        // THEN
        Assert.assertNotNull(result)
        Assert.assertEquals(2, result?.bodyParts?.size)
        Assert.assertEquals(BodyPart.CHEST, result?.bodyParts?.get(0))
        Assert.assertEquals(BodyPart.TRICEPS, result?.bodyParts?.get(1))
        Assert.assertEquals(ExerciseCategory.BARBELL, result?.category)
        Assert.assertEquals(MeasureType.REPS_AND_WEIGHT, result?.measureType)
        Assert.assertEquals("Lie on bench, lower bar to chest, press up", result?.instructions)
        Assert.assertEquals("/images/bench_press.jpg", result?.imagePath)
    }

    @Test
    fun `invoke returns null when exercise not found`() = runTest {
        // GIVEN
        coEvery { repository.getExerciseById("non-existent") } returns flowOf(null)

        // WHEN
        val result = useCase("non-existent").first()

        // THEN
        Assert.assertNull(result)
    }

    @Test
    fun `invoke handles different exercise IDs`() = runTest {
        // GIVEN
        val exercise1 = sampleExercise.copy(id = "exercise-1", name = "Bench Press")
        val exercise2 = sampleExercise.copy(id = "exercise-2", name = "Squat")

        coEvery { repository.getExerciseById("exercise-1") } returns flowOf(exercise1)
        coEvery { repository.getExerciseById("exercise-2") } returns flowOf(exercise2)

        // WHEN
        val result1 = useCase("exercise-1").first()
        val result2 = useCase("exercise-2").first()

        // THEN
        Assert.assertEquals("Bench Press", result1?.name)
        Assert.assertEquals("Squat", result2?.name)
    }

    @Test
    fun `invoke returns exercise with single body part`() = runTest {
        // GIVEN
        val singleBodyPartExercise = sampleExercise.copy(bodyParts = listOf(BodyPart.CHEST))
        coEvery { repository.getExerciseById("exercise-1") } returns flowOf(singleBodyPartExercise)

        // WHEN
        val result = useCase("exercise-1").first()

        // THEN
        Assert.assertEquals(1, result?.bodyParts?.size)
        Assert.assertEquals(BodyPart.CHEST, result?.bodyParts?.first())
    }

    @Test
    fun `invoke returns exercise with multiple body parts`() = runTest {
        // GIVEN
        val multiBodyPartExercise = sampleExercise.copy(
            bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS)
        )
        coEvery { repository.getExerciseById("exercise-1") } returns flowOf(multiBodyPartExercise)

        // WHEN
        val result = useCase("exercise-1").first()

        // THEN
        Assert.assertEquals(3, result?.bodyParts?.size)
    }

    @Test
    fun `invoke returns exercise with different categories`() = runTest {
        // GIVEN
        val barbellExercise = sampleExercise.copy(category = ExerciseCategory.BARBELL)
        val dumbbellExercise =
            sampleExercise.copy(id = "ex-2", category = ExerciseCategory.DUMBBELL)
        val bodyweightExercise =
            sampleExercise.copy(id = "ex-3", category = ExerciseCategory.BODYWEIGHT)

        coEvery { repository.getExerciseById("ex-1") } returns flowOf(barbellExercise)
        coEvery { repository.getExerciseById("ex-2") } returns flowOf(dumbbellExercise)
        coEvery { repository.getExerciseById("ex-3") } returns flowOf(bodyweightExercise)

        // WHEN
        val result1 = useCase("ex-1").first()
        val result2 = useCase("ex-2").first()
        val result3 = useCase("ex-3").first()

        // THEN
        Assert.assertEquals(ExerciseCategory.BARBELL, result1?.category)
        Assert.assertEquals(ExerciseCategory.DUMBBELL, result2?.category)
        Assert.assertEquals(ExerciseCategory.BODYWEIGHT, result3?.category)
    }

    @Test
    fun `invoke returns exercise with different measure types`() = runTest {
        // GIVEN
        val repsAndWeightExercise = sampleExercise.copy(measureType = MeasureType.REPS_AND_WEIGHT)
        val repsOnlyExercise = sampleExercise.copy(id = "ex-2", measureType = MeasureType.REPS_ONLY)
        val timeExercise = sampleExercise.copy(id = "ex-3", measureType = MeasureType.TIME)

        coEvery { repository.getExerciseById("ex-1") } returns flowOf(repsAndWeightExercise)
        coEvery { repository.getExerciseById("ex-2") } returns flowOf(repsOnlyExercise)
        coEvery { repository.getExerciseById("ex-3") } returns flowOf(timeExercise)

        // WHEN
        val result1 = useCase("ex-1").first()
        val result2 = useCase("ex-2").first()
        val result3 = useCase("ex-3").first()

        // THEN
        Assert.assertEquals(MeasureType.REPS_AND_WEIGHT, result1?.measureType)
        Assert.assertEquals(MeasureType.REPS_ONLY, result2?.measureType)
        Assert.assertEquals(MeasureType.TIME, result3?.measureType)
    }

    @Test
    fun `invoke returns exercise without image path`() = runTest {
        // GIVEN
        val exerciseWithoutImage = sampleExercise.copy(imagePath = null)
        coEvery { repository.getExerciseById("exercise-1") } returns flowOf(exerciseWithoutImage)

        // WHEN
        val result = useCase("exercise-1").first()

        // THEN
        Assert.assertNull(result?.imagePath)
    }

    @Test
    fun `invoke returns exercise with empty instructions`() = runTest {
        // GIVEN
        val exerciseWithoutInstructions = sampleExercise.copy(instructions = "")
        coEvery { repository.getExerciseById("exercise-1") } returns flowOf(
            exerciseWithoutInstructions
        )

        // WHEN
        val result = useCase("exercise-1").first()

        // THEN
        Assert.assertEquals("", result?.instructions)
    }

    @Test
    fun `invoke returns flow that emits multiple times`() = runTest {
        // GIVEN
        val flow = flow {
            emit(sampleExercise.copy(name = "Version 1"))
            delay(100)
            emit(sampleExercise.copy(name = "Version 2"))
        }
        coEvery { repository.getExerciseById("exercise-1") } returns flow

        // WHEN
        val results = mutableListOf<Exercise?>()
        useCase("exercise-1").collect { results.add(it) }

        // THEN
        Assert.assertEquals(2, results.size)
        Assert.assertEquals("Version 1", results[0]?.name)
        Assert.assertEquals("Version 2", results[1]?.name)
    }

    @Test
    fun `invoke handles UUID format exercise IDs`() = runTest {
        // GIVEN
        val uuidId = "550e8400-e29b-41d4-a716-446655440000"
        val exercise = sampleExercise.copy(id = uuidId)
        coEvery { repository.getExerciseById(uuidId) } returns flowOf(exercise)

        // WHEN
        val result = useCase(uuidId).first()

        // THEN
        Assert.assertNotNull(result)
        Assert.assertEquals(uuidId, result?.id)
    }
}