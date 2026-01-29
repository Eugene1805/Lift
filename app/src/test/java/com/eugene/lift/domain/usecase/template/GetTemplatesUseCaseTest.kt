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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test for GetTemplatesUseCase
 * Tests retrieval of workout templates with archive filtering
 */
class GetTemplatesUseCaseTest {

    private lateinit var repository: TemplateRepository
    private lateinit var useCase: GetTemplatesUseCase

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
        )
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetTemplatesUseCase(repository)
    }

    @Test
    fun `invoke returns active templates when isArchived is false`() = runTest {
        // GIVEN
        coEvery { repository.getTemplates(false) } returns flowOf(activeTemplates)

        // WHEN
        val result = useCase(isArchived = false).first()

        // THEN
        assertEquals(2, result.size)
        assertTrue(result.all { !it.isArchived })
        assertTrue(result.any { it.name == "Push Day" })
        assertTrue(result.any { it.name == "Pull Day" })
    }

    @Test
    fun `invoke returns archived templates when isArchived is true`() = runTest {
        // GIVEN
        coEvery { repository.getTemplates(true) } returns flowOf(archivedTemplates)

        // WHEN
        val result = useCase(isArchived = true).first()

        // THEN
        assertEquals(1, result.size)
        assertTrue(result.all { it.isArchived })
        assertEquals("Old Routine", result.first().name)
    }

    @Test
    fun `invoke returns active templates by default`() = runTest {
        // GIVEN
        coEvery { repository.getTemplates(false) } returns flowOf(activeTemplates)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(2, result.size)
        assertFalse(result.any { it.isArchived })
    }

    @Test
    fun `invoke returns empty list when no templates exist`() = runTest {
        // GIVEN
        coEvery { repository.getTemplates(false) } returns flowOf(emptyList())

        // WHEN
        val result = useCase(isArchived = false).first()

        // THEN
        assertEquals(0, result.size)
    }

    @Test
    fun `invoke returns templates with all properties`() = runTest {
        // GIVEN
        val templateWithDetails = WorkoutTemplate(
            id = "template-1",
            name = "Push Day",
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
        coEvery { repository.getTemplates(false) } returns flowOf(listOf(templateWithDetails))

        // WHEN
        val result = useCase(isArchived = false).first()

        // THEN
        val template = result.first()
        assertEquals("Important notes", template.notes)
        assertEquals(1, template.exercises.size)
        assertEquals("folder-1", template.folderId)
    }

    @Test
    fun `invoke handles large number of templates`() = runTest {
        // GIVEN
        val manyTemplates = (1..100).map { index ->
            WorkoutTemplate(
                id = "template-$index",
                name = "Template $index",
                notes = "",
                exercises = emptyList(),
                isArchived = false
            )
        }
        coEvery { repository.getTemplates(false) } returns flowOf(manyTemplates)

        // WHEN
        val result = useCase(isArchived = false).first()

        // THEN
        assertEquals(100, result.size)
    }

    @Test
    fun `invoke returns templates in order from repository`() = runTest {
        // GIVEN
        val orderedTemplates = listOf(
            WorkoutTemplate(id = "1", name = "First", notes = "", exercises = emptyList(), isArchived = false),
            WorkoutTemplate(id = "2", name = "Second", notes = "", exercises = emptyList(), isArchived = false),
            WorkoutTemplate(id = "3", name = "Third", notes = "", exercises = emptyList(), isArchived = false)
        )
        coEvery { repository.getTemplates(false) } returns flowOf(orderedTemplates)

        // WHEN
        val result = useCase(isArchived = false).first()

        // THEN
        assertEquals("First", result[0].name)
        assertEquals("Second", result[1].name)
        assertEquals("Third", result[2].name)
    }

    @Test
    fun `invoke returns flow that emits multiple times`() = runTest {
        // GIVEN
        val flow = kotlinx.coroutines.flow.flow {
            emit(listOf(activeTemplates[0]))
            kotlinx.coroutines.delay(100)
            emit(activeTemplates)
        }
        coEvery { repository.getTemplates(false) } returns flow

        // WHEN
        val results = mutableListOf<List<WorkoutTemplate>>()
        useCase(isArchived = false).collect { results.add(it) }

        // THEN
        assertEquals(2, results.size)
        assertEquals(1, results[0].size)
        assertEquals(2, results[1].size)
    }
}
