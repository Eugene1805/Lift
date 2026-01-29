package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.TemplateExercise
import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.repository.TemplateRepository
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit test for SaveTemplateUseCase
 * Tests validation and saving of workout templates
 */
class SaveTemplateUseCaseTest {

    private lateinit var repository: TemplateRepository
    private lateinit var useCase: SaveTemplateUseCase

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
                id = "template-ex-1",
                exercise = sampleExercise,
                orderIndex = 0,
                targetSets = 3,
                targetReps = "10",
                restTimerSeconds = 90,
                note = ""
            )
        )
    )

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = SaveTemplateUseCase(repository)
    }

    @Test
    fun `invoke saves valid template`() = runTest {
        // GIVEN
        val template = sampleTemplate

        // WHEN
        useCase(template)

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val savedTemplate = slot.captured
        assertEquals("Push Day", savedTemplate.name)
        assertEquals(1, savedTemplate.exercises.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke throws exception when name is blank`() = runTest {
        // GIVEN
        val template = sampleTemplate.copy(name = "")

        // WHEN - Should throw exception
        useCase(template)

        // THEN - Exception expected
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke throws exception when name is only whitespace`() = runTest {
        // GIVEN
        val template = sampleTemplate.copy(name = "   ")

        // WHEN - Should throw exception
        useCase(template)

        // THEN - Exception expected
    }

    @Test
    fun `invoke saves template with empty exercises list`() = runTest {
        // GIVEN - Empty exercises is allowed (draft templates)
        val template = sampleTemplate.copy(exercises = emptyList())

        // WHEN
        useCase(template)

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val savedTemplate = slot.captured
        assertEquals(0, savedTemplate.exercises.size)
    }

    @Test
    fun `invoke saves template with multiple exercises`() = runTest {
        // GIVEN
        val exercises = (1..5).map { index ->
            TemplateExercise(
                id = "template-ex-$index",
                exercise = sampleExercise.copy(id = "exercise-$index", name = "Exercise $index"),
                orderIndex = index - 1,
                targetSets = 3,
                targetReps = "10",
                restTimerSeconds = 60,
                note = ""
            )
        }
        val template = sampleTemplate.copy(exercises = exercises)

        // WHEN
        useCase(template)

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val savedTemplate = slot.captured
        assertEquals(5, savedTemplate.exercises.size)
    }

    @Test
    fun `invoke saves template with all properties`() = runTest {
        // GIVEN
        val template = sampleTemplate.copy(
            notes = "Important notes",
            isArchived = true,
            folderId = "folder-1"
        )

        // WHEN
        useCase(template)

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val savedTemplate = slot.captured
        assertEquals("Important notes", savedTemplate.notes)
        assertEquals(true, savedTemplate.isArchived)
        assertEquals("folder-1", savedTemplate.folderId)
    }

    @Test
    fun `invoke preserves exercise order`() = runTest {
        // GIVEN
        val exercises = listOf(
            TemplateExercise(
                id = "ex-1",
                exercise = sampleExercise.copy(name = "First"),
                orderIndex = 0,
                targetSets = 3,
                targetReps = "10",
                restTimerSeconds = 60,
                note = ""
            ),
            TemplateExercise(
                id = "ex-2",
                exercise = sampleExercise.copy(name = "Second"),
                orderIndex = 1,
                targetSets = 3,
                targetReps = "10",
                restTimerSeconds = 60,
                note = ""
            ),
            TemplateExercise(
                id = "ex-3",
                exercise = sampleExercise.copy(name = "Third"),
                orderIndex = 2,
                targetSets = 3,
                targetReps = "10",
                restTimerSeconds = 60,
                note = ""
            )
        )
        val template = sampleTemplate.copy(exercises = exercises)

        // WHEN
        useCase(template)

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val savedTemplate = slot.captured
        assertEquals("First", savedTemplate.exercises[0].exercise.name)
        assertEquals("Second", savedTemplate.exercises[1].exercise.name)
        assertEquals("Third", savedTemplate.exercises[2].exercise.name)
    }

    @Test
    fun `invoke saves template for update scenario`() = runTest {
        // GIVEN - Existing template being updated
        val updatedTemplate = sampleTemplate.copy(
            name = "Updated Push Day",
            notes = "Updated notes"
        )

        // WHEN
        useCase(updatedTemplate)

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val savedTemplate = slot.captured
        assertEquals("template-1", savedTemplate.id) // Same ID
        assertEquals("Updated Push Day", savedTemplate.name) // Updated name
    }

    @Test
    fun `invoke handles template with long name`() = runTest {
        // GIVEN
        val longName = "A".repeat(100)
        val template = sampleTemplate.copy(name = longName)

        // WHEN
        useCase(template)

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val savedTemplate = slot.captured
        assertEquals(longName, savedTemplate.name)
    }

    @Test
    fun `invoke handles template with special characters in name`() = runTest {
        // GIVEN
        val specialName = "Push Day 💪 (Week 1) #1"
        val template = sampleTemplate.copy(name = specialName)

        // WHEN
        useCase(template)

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val savedTemplate = slot.captured
        assertEquals(specialName, savedTemplate.name)
    }
}
