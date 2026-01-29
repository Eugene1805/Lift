package com.eugene.lift.domain.usecase.folder

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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Unit test for MoveTemplateToFolderUseCase
 * Tests moving templates to folders or root
 */
class MoveTemplateToFolderUseCaseTest {

    private lateinit var repository: TemplateRepository
    private lateinit var useCase: MoveTemplateToFolderUseCase

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
        notes = "Test notes",
        exercises = listOf(
            TemplateExercise(
                id = "ex-1",
                exercise = sampleExercise,
                orderIndex = 0,
                targetSets = 3,
                targetReps = "10",
                restTimerSeconds = 90,
                note = ""
            )
        ),
        folderId = null // Initially at root
    )

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = MoveTemplateToFolderUseCase(repository)
    }

    @Test
    fun `invoke moves template to folder`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN
        useCase("template-1", "folder-1")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val movedTemplate = slot.captured
        assertEquals("folder-1", movedTemplate.folderId)
    }

    @Test
    fun `invoke moves template to root when folderId is null`() = runTest {
        // GIVEN
        val templateInFolder = sampleTemplate.copy(folderId = "folder-1")
        coEvery { repository.getTemplate("template-1") } returns flowOf(templateInFolder)

        // WHEN
        useCase("template-1", null)

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val movedTemplate = slot.captured
        assertNull(movedTemplate.folderId)
    }

    @Test
    fun `invoke preserves all other template properties`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN
        useCase("template-1", "folder-1")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val movedTemplate = slot.captured
        assertEquals("template-1", movedTemplate.id)
        assertEquals("Push Day", movedTemplate.name)
        assertEquals("Test notes", movedTemplate.notes)
        assertEquals(1, movedTemplate.exercises.size)
        assertEquals(sampleTemplate.isArchived, movedTemplate.isArchived)
    }

    @Test
    fun `invoke does nothing when template not found`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("non-existent") } returns flowOf(null)

        // WHEN
        useCase("non-existent", "folder-1")

        // THEN
        coVerify(exactly = 0) { repository.saveTemplate(any()) }
    }

    @Test
    fun `invoke handles moving template between folders`() = runTest {
        // GIVEN
        val templateInFolder1 = sampleTemplate.copy(folderId = "folder-1")
        coEvery { repository.getTemplate("template-1") } returns flowOf(templateInFolder1)

        // WHEN
        useCase("template-1", "folder-2")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val movedTemplate = slot.captured
        assertEquals("folder-2", movedTemplate.folderId)
    }

    @Test
    fun `invoke handles moving multiple templates sequentially`() = runTest {
        // GIVEN
        val template1 = sampleTemplate.copy(id = "template-1")
        val template2 = sampleTemplate.copy(id = "template-2")
        val template3 = sampleTemplate.copy(id = "template-3")

        coEvery { repository.getTemplate("template-1") } returns flowOf(template1)
        coEvery { repository.getTemplate("template-2") } returns flowOf(template2)
        coEvery { repository.getTemplate("template-3") } returns flowOf(template3)

        // WHEN
        useCase("template-1", "folder-1")
        useCase("template-2", "folder-1")
        useCase("template-3", "folder-2")

        // THEN
        coVerify(exactly = 3) { repository.saveTemplate(any()) }
    }

    @Test
    fun `invoke handles moving archived template`() = runTest {
        // GIVEN
        val archivedTemplate = sampleTemplate.copy(isArchived = true)
        coEvery { repository.getTemplate("template-1") } returns flowOf(archivedTemplate)

        // WHEN
        useCase("template-1", "folder-1")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val movedTemplate = slot.captured
        assertEquals("folder-1", movedTemplate.folderId)
        assertEquals(true, movedTemplate.isArchived) // Archived state preserved
    }

    @Test
    fun `invoke handles moving template with lastPerformedAt`() = runTest {
        // GIVEN
        val performedDate = java.time.LocalDateTime.now()
        val templateWithDate = sampleTemplate.copy(lastPerformedAt = performedDate)
        coEvery { repository.getTemplate("template-1") } returns flowOf(templateWithDate)

        // WHEN
        useCase("template-1", "folder-1")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val movedTemplate = slot.captured
        assertEquals(performedDate, movedTemplate.lastPerformedAt)
    }

    @Test
    fun `invoke handles moving template with many exercises`() = runTest {
        // GIVEN
        val manyExercises = (1..10).map { index ->
            TemplateExercise(
                id = "ex-$index",
                exercise = sampleExercise.copy(id = "exercise-$index"),
                orderIndex = index - 1,
                targetSets = 3,
                targetReps = "10",
                restTimerSeconds = 60,
                note = ""
            )
        }
        val largeTemplate = sampleTemplate.copy(exercises = manyExercises)
        coEvery { repository.getTemplate("template-1") } returns flowOf(largeTemplate)

        // WHEN
        useCase("template-1", "folder-1")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val movedTemplate = slot.captured
        assertEquals(10, movedTemplate.exercises.size)
        assertEquals("folder-1", movedTemplate.folderId)
    }

    @Test
    fun `invoke handles moving template with empty string folder ID`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("template-1") } returns flowOf(sampleTemplate)

        // WHEN - Empty string is different from null
        useCase("template-1", "")

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val movedTemplate = slot.captured
        assertEquals("", movedTemplate.folderId)
    }

    @Test
    fun `invoke handles UUID format folder IDs`() = runTest {
        // GIVEN
        coEvery { repository.getTemplate("template-1") } returns flowOf(sampleTemplate)
        val uuidFolderId = "550e8400-e29b-41d4-a716-446655440000"

        // WHEN
        useCase("template-1", uuidFolderId)

        // THEN
        val slot = slot<WorkoutTemplate>()
        coVerify(exactly = 1) { repository.saveTemplate(capture(slot)) }

        val movedTemplate = slot.captured
        assertEquals(uuidFolderId, movedTemplate.folderId)
    }
}
