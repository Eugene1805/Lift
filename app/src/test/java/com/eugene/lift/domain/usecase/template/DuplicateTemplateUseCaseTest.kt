package com.eugene.lift.domain.usecase.template

import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.TemplateExercise
import com.eugene.lift.domain.model.WorkoutTemplate
import com.eugene.lift.domain.repository.TemplateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test for DuplicateTemplateUseCase
 * Tests template duplication with ID regeneration and name modification
 */
class DuplicateTemplateUseCaseTest {

    private lateinit var repository: TemplateRepository
    private lateinit var useCase: DuplicateTemplateUseCase

    private val sampleExercise = Exercise(
        id = "exercise-1",
        name = "Bench Press",
        bodyParts = listOf(BodyPart.CHEST),
        category = ExerciseCategory.BARBELL,
        measureType = MeasureType.REPS_AND_WEIGHT,
        instructions = "",
        imagePath = null
    )

    private val originalTemplate = WorkoutTemplate(
        id = "original-template",
        name = "Push Day",
        notes = "Original notes",
        exercises = listOf(
            TemplateExercise(
                id = "ex-1",
                exercise = sampleExercise,
                orderIndex = 0,
                targetSets = 3,
                targetReps = "10",
                restTimerSeconds = 90,
                note = "Exercise note"
            ),
            TemplateExercise(
                id = "ex-2",
                exercise = sampleExercise.copy(id = "exercise-2", name = "Incline Press"),
                orderIndex = 1,
                targetSets = 4,
                targetReps = "8",
                restTimerSeconds = 120,
                note = ""
            )
        ),
        isArchived = false,
        lastPerformedAt = java.time.LocalDateTime.now(),
        folderId = "folder-1"
    )

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = DuplicateTemplateUseCase(repository)
    }

    @Test
    fun `invoke creates duplicate with modified name`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("original-template") } returns flowOf(originalTemplate)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        assertEquals("Push Day (Copia)", duplicate.name)
    }

    @Test
    fun `invoke generates new template ID`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("original-template") } returns flowOf(originalTemplate)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        assertNotEquals("original-template", duplicate.id)
        assertTrue(duplicate.id.isNotEmpty())
    }

    @Test
    fun `invoke generates new IDs for all exercises`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("original-template") } returns flowOf(originalTemplate)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        val originalExerciseIds = originalTemplate.exercises.map { it.id }
        val duplicateExerciseIds = duplicate.exercises.map { it.id }

        // None of the duplicate IDs should match original IDs
        duplicateExerciseIds.forEach { duplicateId ->
            assertFalse(originalExerciseIds.contains(duplicateId))
        }

        // All duplicate IDs should be unique
        assertEquals(duplicateExerciseIds.size, duplicateExerciseIds.distinct().size)
    }

    @Test
    fun `invoke preserves exercise order`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("original-template") } returns flowOf(originalTemplate)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        assertEquals(2, duplicate.exercises.size)
        assertEquals("Bench Press", duplicate.exercises[0].exercise.name)
        assertEquals("Incline Press", duplicate.exercises[1].exercise.name)
        assertEquals(0, duplicate.exercises[0].orderIndex)
        assertEquals(1, duplicate.exercises[1].orderIndex)
    }

    @Test
    fun `invoke preserves exercise properties`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("original-template") } returns flowOf(originalTemplate)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        val duplicateExercise = duplicate.exercises[0]
        val originalExercise = originalTemplate.exercises[0]

        assertEquals(originalExercise.targetSets, duplicateExercise.targetSets)
        assertEquals(originalExercise.targetReps, duplicateExercise.targetReps)
        assertEquals(originalExercise.restTimerSeconds, duplicateExercise.restTimerSeconds)
        assertEquals(originalExercise.note, duplicateExercise.note)
    }

    @Test
    fun `invoke sets isArchived to false`() = runTest {
        // GIVEN - Original is archived
        val archivedTemplate = originalTemplate.copy(isArchived = true)
        coEvery { repository.getTemplate("original-template") } returns flowOf(archivedTemplate)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        assertFalse(duplicate.isArchived)
    }

    @Test
    fun `invoke clears lastPerformedAt`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("original-template") } returns flowOf(originalTemplate)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        assertNull(duplicate.lastPerformedAt)
    }

    @Test
    fun `invoke preserves notes`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("original-template") } returns flowOf(originalTemplate)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        assertEquals("Original notes", duplicate.notes)
    }

    @Test
    fun `invoke preserves folderId`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("original-template") } returns flowOf(originalTemplate)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        assertEquals("folder-1", duplicate.folderId)
    }

    @Test
    fun `invoke handles template with no exercises`() = runTest {
        // GIVEN
        val emptyTemplate = originalTemplate.copy(exercises = emptyList())
        coEvery { repository.getTemplate("original-template") } returns flowOf(emptyTemplate)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        assertEquals(0, duplicate.exercises.size)
        assertEquals("Push Day (Copia)", duplicate.name)
    }

    @Test
    fun `invoke does nothing when template not found`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("non-existent") } returns flowOf(null)

        // WHEN
        useCase("non-existent")

        // THEN
        coVerify(exactly = 0) { repository.saveTemplate(any()) }
    }

    @Test
    fun `invoke handles template with single exercise`() = runTest {
        // GIVEN
        val singleExerciseTemplate = originalTemplate.copy(
            exercises = listOf(originalTemplate.exercises.first())
        )
        coEvery { repository.getTemplate("original-template") } returns flowOf(singleExerciseTemplate)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        assertEquals(1, duplicate.exercises.size)
        assertNotEquals(singleExerciseTemplate.exercises[0].id, duplicate.exercises[0].id)
    }

    @Test
    fun `invoke handles template with many exercises`() = runTest {
        // GIVEN
        val manyExercises = (1..10).map { index ->
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
        val largeTemplate = originalTemplate.copy(exercises = manyExercises)
        coEvery { repository.getTemplate("original-template") } returns flowOf(largeTemplate)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        assertEquals(10, duplicate.exercises.size)

        // All exercise IDs should be new
        val originalIds = manyExercises.map { it.id }
        val duplicateIds = duplicate.exercises.map { it.id }
        duplicateIds.forEach { duplicateId ->
            assertFalse(originalIds.contains(duplicateId))
        }
    }

    @Test
    fun `invoke creates duplicate with name already containing Copia`() = runTest {
        // GIVEN
        val templateWithCopia = originalTemplate.copy(name = "Push Day (Copia)")
        coEvery { repository.getTemplate("original-template") } returns flowOf(templateWithCopia)

        // WHEN
        useCase("original-template")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val duplicate = slot.captured
        assertEquals("Push Day (Copia) (Copia)", duplicate.name)
    }
}
