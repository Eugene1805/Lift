package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.TemplateExercise
import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.repository.TemplateRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Unit test for GetTemplateDetailUseCase
 * Tests retrieval of template details by ID
 */
class GetTemplateDetailUseCaseTest {

    private lateinit var repository: TemplateRepository
    private lateinit var useCase: GetTemplateDetailUseCase

    private val sampleExercise = Exercise(
        id = "exercise-1",
        name = "Bench Press",
        bodyParts = listOf(BodyPart.CHEST),
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
                id = "ex-1",
                exercise = sampleExercise,
                orderIndex = 0,
                targetSets = 3,
                targetReps = "10",
                restTimerSeconds = 90,
                note = "Exercise note"
            )
        ),
        isArchived = false,
        lastPerformedAt = java.time.LocalDateTime.now(),
        folderId = "folder-1"
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetTemplateDetailUseCase(repository)
    }

    @Test
    fun `invoke returns template by ID`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN
        val result = useCase("template-1").first()

        // THEN
        assertNotNull(result)
        assertEquals("template-1", result?.id)
        assertEquals("Push Day", result?.name)
    }

    @Test
    fun `invoke returns template with all properties`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN
        val result = useCase("template-1").first()

        // THEN
        assertNotNull(result)
        assertEquals("Chest and triceps workout", result?.notes)
        assertEquals(1, result?.exercises?.size)
        assertEquals(false, result?.isArchived)
        assertEquals("folder-1", result?.folderId)
        assertNotNull(result?.lastPerformedAt)
    }

    @Test
    fun `invoke returns template with exercise details`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN
        val result = useCase("template-1").first()

        // THEN
        val exercise = result?.exercises?.first()
        assertNotNull(exercise)
        assertEquals("Bench Press", exercise?.exercise?.name)
        assertEquals(3, exercise?.targetSets)
        assertEquals("10", exercise?.targetReps)
        assertEquals(90, exercise?.restTimerSeconds)
        assertEquals("Exercise note", exercise?.note)
    }

    @Test
    fun `invoke returns null when template not found`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("non-existent") } returns flowOf(null)

        // WHEN
        val result = useCase("non-existent").first()

        // THEN
        assertNull(result)
    }

    @Test
    fun `invoke handles different template IDs`() = runTest {
        // GIVEN
        val template1 = sampleTemplate.copy(id = "template-1", name = "Template 1")
        val template2 = sampleTemplate.copy(id = "template-2", name = "Template 2")

        coEvery { repository.getTemplate("template-1") } returns flowOf(template1)
        coEvery { repository.getTemplate("template-2") } returns flowOf(template2)

        // WHEN
        val result1 = useCase("template-1").first()
        val result2 = useCase("template-2").first()

        // THEN
        assertEquals("Template 1", result1?.name)
        assertEquals("Template 2", result2?.name)
    }

    @Test
    fun `invoke returns template with multiple exercises`() = runTest {
        // GIVEN
        val multiExerciseTemplate = sampleTemplate.copy(
            exercises = (1..5).map { index ->
                TemplateExercise(
                    id = "ex-$index",
                    exercise = sampleExercise.copy(id = "exercise-$index", name = "Exercise $index"),
                    orderIndex = index - 1,
                    targetSets = 3,
                    targetReps = "10",
                    restTimerSeconds = 60,
                    note = ""
                )
            }
        )
        coEvery { repository.getTemplate("template-1") } returns flowOf(multiExerciseTemplate)

        // WHEN
        val result = useCase("template-1").first()

        // THEN
        assertEquals(5, result?.exercises?.size)
        assertEquals("Exercise 1", result?.exercises?.get(0)?.exercise?.name)
        assertEquals("Exercise 5", result?.exercises?.get(4)?.exercise?.name)
    }

    @Test
    fun `invoke returns archived template`() = runTest {
        // GIVEN
        val archivedTemplate = sampleTemplate.copy(isArchived = true)
        coEvery { repository.getTemplate("template-1") } returns flowOf(archivedTemplate)

        // WHEN
        val result = useCase("template-1").first()

        // THEN
        assertEquals(true, result?.isArchived)
    }

    @Test
    fun `invoke returns template without folder`() = runTest {
        // GIVEN
        val templateWithoutFolder = sampleTemplate.copy(folderId = null)
        coEvery { repository.getTemplate("template-1") } returns flowOf(templateWithoutFolder)

        // WHEN
        val result = useCase("template-1").first()

        // THEN
        assertNull(result?.folderId)
    }

    @Test
    fun `invoke returns flow that emits multiple times`() = runTest {
        // GIVEN
        val flow = kotlinx.coroutines.flow.flow {
            emit(sampleTemplate.copy(name = "Version 1"))
            kotlinx.coroutines.delay(100)
            emit(sampleTemplate.copy(name = "Version 2"))
        }
        coEvery { repository.getTemplate("template-1") } returns flow

        // WHEN
        val results = mutableListOf<WorkoutTemplate?>()
        useCase("template-1").collect { results.add(it) }

        // THEN
        assertEquals(2, results.size)
        assertEquals("Version 1", results[0]?.name)
        assertEquals("Version 2", results[1]?.name)
    }
}
