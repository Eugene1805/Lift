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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test for GetAllTemplatesUseCase
 * Tests retrieval of all templates (active and archived combined)
 */
class GetAllTemplatesUseCaseTest {

    private lateinit var repository: TemplateRepository
    private lateinit var useCase: GetAllTemplatesUseCase

    private val sampleExercise = Exercise(
        id = "exercise-1",
        name = "Bench Press",
        bodyParts = listOf(BodyPart.CHEST),
        category = ExerciseCategory.BARBELL,
        measureType = MeasureType.REPS_AND_WEIGHT,
        instructions = "",
        imagePath = null
    )

    private val activeTemplates = listOf(
        WorkoutTemplate(
            id = "template-1",
            name = "Push Day",
            notes = "",
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
            isArchived = false
        ),
        WorkoutTemplate(
            id = "template-2",
            name = "Pull Day",
            notes = "",
            exercises = emptyList(),
            isArchived = false
        )
    )

    private val archivedTemplates = listOf(
        WorkoutTemplate(
            id = "template-3",
            name = "Old Routine",
            notes = "",
            exercises = emptyList(),
            isArchived = true
        ),
        WorkoutTemplate(
            id = "template-4",
            name = "Archived Workout",
            notes = "",
            exercises = emptyList(),
            isArchived = true
        )
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetAllTemplatesUseCase(repository)
    }

    @Test
    fun `invoke returns all active and archived templates combined`() = runTest {
        // GIVEN
        coEvery { repository.getTemplates(false) } returns flowOf(activeTemplates)
        coEvery { repository.getTemplates(true) } returns flowOf(archivedTemplates)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(4, result.size)
        assertTrue(result.any { it.name == "Push Day" })
        assertTrue(result.any { it.name == "Pull Day" })
        assertTrue(result.any { it.name == "Old Routine" })
        assertTrue(result.any { it.name == "Archived Workout" })
    }

    @Test
    fun `invoke returns only active templates when no archived exist`() = runTest {
        // GIVEN
        coEvery { repository.getTemplates(false) } returns flowOf(activeTemplates)
        coEvery { repository.getTemplates(true) } returns flowOf(emptyList())

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(2, result.size)
        assertTrue(result.all { !it.isArchived })
    }

    @Test
    fun `invoke returns only archived templates when no active exist`() = runTest {
        // GIVEN
        coEvery { repository.getTemplates(false) } returns flowOf(emptyList())
        coEvery { repository.getTemplates(true) } returns flowOf(archivedTemplates)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(2, result.size)
        assertTrue(result.all { it.isArchived })
    }

    @Test
    fun `invoke returns empty list when no templates exist`() = runTest {
        // GIVEN
        coEvery { repository.getTemplates(false) } returns flowOf(emptyList())
        coEvery { repository.getTemplates(true) } returns flowOf(emptyList())

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(0, result.size)
    }

    @Test
    fun `invoke combines templates in correct order`() = runTest {
        // GIVEN
        coEvery { repository.getTemplates(false) } returns flowOf(activeTemplates)
        coEvery { repository.getTemplates(true) } returns flowOf(archivedTemplates)

        // WHEN
        val result = useCase().first()

        // THEN
        // Active templates should come first
        assertEquals("Push Day", result[0].name)
        assertEquals("Pull Day", result[1].name)
        // Archived templates should come after
        assertEquals("Old Routine", result[2].name)
        assertEquals("Archived Workout", result[3].name)
    }

    @Test
    fun `invoke handles large number of templates`() = runTest {
        // GIVEN
        val manyActiveTemplates = (1..50).map { index ->
            WorkoutTemplate(
                id = "active-$index",
                name = "Active $index",
                notes = "",
                exercises = emptyList(),
                isArchived = false
            )
        }
        val manyArchivedTemplates = (1..50).map { index ->
            WorkoutTemplate(
                id = "archived-$index",
                name = "Archived $index",
                notes = "",
                exercises = emptyList(),
                isArchived = true
            )
        }
        coEvery { repository.getTemplates(false) } returns flowOf(manyActiveTemplates)
        coEvery { repository.getTemplates(true) } returns flowOf(manyArchivedTemplates)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(100, result.size)
    }

    @Test
    fun `invoke emits new result when either flow updates`() = runTest {
        // GIVEN
        val activeFlow = kotlinx.coroutines.flow.flow {
            emit(listOf(activeTemplates[0]))
            kotlinx.coroutines.delay(100)
            emit(activeTemplates)
        }
        val archivedFlow = flowOf(archivedTemplates)

        coEvery { repository.getTemplates(false) } returns activeFlow
        coEvery { repository.getTemplates(true) } returns archivedFlow

        // WHEN
        val results = mutableListOf<List<WorkoutTemplate>>()
        useCase().collect {
            results.add(it)
            if (results.size >= 2) return@collect
        }

        // THEN
        assertEquals(2, results.size)
        assertEquals(3, results[0].size) // 1 active + 2 archived
        assertEquals(4, results[1].size) // 2 active + 2 archived
    }

    @Test
    fun `invoke preserves all template properties`() = runTest {
        // GIVEN
        val detailedTemplate = WorkoutTemplate(
            id = "template-1",
            name = "Detailed Template",
            notes = "Important notes",
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
        coEvery { repository.getTemplates(false) } returns flowOf(listOf(detailedTemplate))
        coEvery { repository.getTemplates(true) } returns flowOf(emptyList())

        // WHEN
        val result = useCase().first()

        // THEN
        val template = result.first()
        assertEquals("Detailed Template", template.name)
        assertEquals("Important notes", template.notes)
        assertEquals(1, template.exercises.size)
        assertEquals("folder-1", template.folderId)
    }

    @Test
    fun `invoke handles single active template`() = runTest {
        // GIVEN
        coEvery { repository.getTemplates(false) } returns flowOf(listOf(activeTemplates[0]))
        coEvery { repository.getTemplates(true) } returns flowOf(emptyList())

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(1, result.size)
        assertEquals("Push Day", result.first().name)
    }

    @Test
    fun `invoke handles single archived template`() = runTest {
        // GIVEN
        coEvery { repository.getTemplates(false) } returns flowOf(emptyList())
        coEvery { repository.getTemplates(true) } returns flowOf(listOf(archivedTemplates[0]))

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(1, result.size)
        assertEquals("Old Routine", result.first().name)
    }
}
