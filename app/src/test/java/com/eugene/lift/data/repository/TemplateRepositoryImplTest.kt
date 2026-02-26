package com.eugene.lift.data.repository

import com.eugene.lift.data.local.dao.TemplateDao
import com.eugene.lift.data.local.dao.TemplateWithExercises
import com.eugene.lift.data.local.entity.WorkoutTemplateEntity
import com.eugene.lift.domain.model.WorkoutTemplate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class TemplateRepositoryImplTest {

    private lateinit var dao: TemplateDao
    private lateinit var repository: TemplateRepositoryImpl

    @Before
    fun setup() {
        dao = mockk()
        repository = TemplateRepositoryImpl(dao)
    }

    @Test
    fun `getTemplates maps TemplateWithExercises to Domain properly`() = runTest {
        val entity = WorkoutTemplateEntity(
            id = "t1",
            name = "Test Template",
            notes = "",
            isArchived = false,
            sortOrder = 0,
            lastPerformedAt = null
        )
        val templateWithExercises = TemplateWithExercises(
            template = entity,
            exercises = emptyList()
        )
        
        coEvery { dao.getTemplates(false) } returns flowOf(listOf(templateWithExercises))

        val result = repository.getTemplates(false).first()

        assertEquals(1, result.size)
        assertEquals("Test Template", result.first().name)
    }

    @Test
    fun `getTemplate by ID returns correctly mapped domain template`() = runTest {
        val entity = WorkoutTemplateEntity(
            id = "t1",
            name = "Test Template 2",
            notes = "",
            isArchived = false,
            sortOrder = 0,
            lastPerformedAt = null
        )
        val templateWithExercises = TemplateWithExercises(
            template = entity,
            exercises = emptyList()
        )
        
        coEvery { dao.getTemplateById("t1") } returns flowOf(templateWithExercises)

        val result = repository.getTemplate("t1").first()

        assertEquals("Test Template 2", result?.name)
    }

    @Test
    fun `getTemplate by ID returns null if dao returns null`() = runTest {
        coEvery { dao.getTemplateById("t1") } returns flowOf(null)
        val result = repository.getTemplate("t1").first()
        assertNull(result)
    }

    @Test
    fun `saveTemplate calls dao saveTemplateComplete`() = runTest {
        val template = WorkoutTemplate(
            id = "t1",
            name = "New Template",
            notes = "",
            exercises = emptyList()
        )

        coEvery { dao.saveTemplateComplete(any(), any()) } returns Unit

        repository.saveTemplate(template)

        coVerify { 
            dao.saveTemplateComplete(
                match { it.id == "t1" && it.name == "New Template" },
                any() // Should be emptyList() since exercises are empty
            )
        }
    }

    @Test
    fun `archiveTemplate calls dao setArchived`() = runTest {
        coEvery { dao.setArchived("t1", true) } returns Unit

        repository.archiveTemplate("t1", true)

        coVerify { dao.setArchived("t1", true) }
    }

    @Test
    fun `deleteTemplate calls dao deleteTemplate`() = runTest {
        coEvery { dao.deleteTemplate("t1") } returns Unit

        repository.deleteTemplate("t1")

        coVerify { dao.deleteTemplate("t1") }
    }
}
